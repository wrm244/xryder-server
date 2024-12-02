package cn.xryder.base.service.system;

import cn.xryder.base.domain.dto.system.DeptDTO;
import cn.xryder.base.domain.entity.system.Department;
import cn.xryder.base.domain.entity.system.Position;
import cn.xryder.base.domain.vo.DepartmentVO;
import cn.xryder.base.exception.custom.BadRequestException;
import cn.xryder.base.exception.custom.NotAllowedException;
import cn.xryder.base.exception.custom.ResourceConflictException;
import cn.xryder.base.exception.custom.ResourceNotFoundException;
import cn.xryder.base.repo.system.DepartmentRepo;
import cn.xryder.base.repo.system.PositionRepo;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: joetao
 * @Date: 2024/8/2 10:43
 */
@Service
@Slf4j
public class DepartmentServiceImpl implements DepartmentService {
    private final DepartmentRepo departmentRepo;
    private final PositionRepo positionRepo;

    public DepartmentServiceImpl(DepartmentRepo departmentRepo, PositionRepo positionRepo) {
        this.departmentRepo = departmentRepo;
        this.positionRepo = positionRepo;
    }

    @Override
    public DepartmentVO getAllDepartments(String q) {
        Specification<Department> spec = searchByQuery(q);
        List<Department> all = departmentRepo.findAll(spec);
        if (all.size() == 0) {
            return null;
        }
        if (StringUtils.isEmpty(q)) {
            return convertToVO(all.getFirst());
        }
        //根据查询出的部门，获取这个部门的上级部门
        Set<Department> resultSet = new HashSet<>();

        for (Department department : all) {
            resultSet.add(department);
            fetchAllDepartments(department, resultSet);
        }
        Department department = buildDepartmentTree(resultSet);
        return convertToVO(department);
    }

    @Override
    public DepartmentVO getDepartmentById(Long id) {
        Department department = departmentRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found！"));
        return convertToVO(department);
    }

    @Override
    public DepartmentVO createDepartment(Department department, String username) {
        //同一部门下，不能出现同名的子部门名称
        Department parentDepartment1 = department.getParentDepartment();
        if (parentDepartment1 == null) {
            List<Department> topDepartments = departmentRepo.findDepartmentsByParentDepartmentIsNull();
            List<String> childNames = topDepartments.stream().map(Department::getName).toList();
            if (childNames.contains(department.getName())) {
                throw new ResourceConflictException("存在相同名称的部门！");
            }
        } else {
            Long parentId = department.getParentDepartment().getId();
            if (parentId == null) {
                throw new BadRequestException("上级部门id不能为空！");
            }
            Optional<Department> parentDepartmentOptional = departmentRepo.findById(parentId);
            if (parentDepartmentOptional.isPresent()) {
                Department parentDepartment = parentDepartmentOptional.get();
                List<String> childNames = parentDepartment.getChildrenDepartments().stream().map(Department::getName).toList();
                if (childNames.contains(department.getName())) {
                    throw new ResourceConflictException("存在相同名称的部门！");
                }
            }
        }
        int defaultPosition = 5;
        department.setPosition(defaultPosition);
        department.setCreateTime(LocalDateTime.now());
        department.setCreator(username);
        departmentRepo.save(department);
        return convertToVO(department);
    }

    @Override
    public DepartmentVO moveDepartment(Long id, Long parentId) {
        Optional<Department> parentDepartmentOptional = departmentRepo.findById(parentId);
        Department department = departmentRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("部门不存在！"));
        if (parentDepartmentOptional.isPresent()) {
            Department parentDepartment = parentDepartmentOptional.get();
            List<String> childNames = parentDepartment.getChildrenDepartments()
                    .stream()
                    .filter(cd -> !Objects.equals(cd.getId(), id))
                    .map(Department::getName).toList();
            if (childNames.contains(department.getName())) {
                throw new ResourceConflictException("存在相同名称的部门！");
            }
        }
        department.getParentDepartment().setId(parentId);
        departmentRepo.save(department);
        return convertToVO(department);
    }

    @Override
    public DepartmentVO updateDepartment(Long id, DeptDTO deptDTO) {
        Department department = departmentRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("部门不存在！"));
        List<Department> brotherDepartments = departmentRepo.findDepartmentsByParentDepartment(department.getParentDepartment());
        List<String> broNames = brotherDepartments.stream().filter(d -> !Objects.equals(d.getId(), id)).map(Department::getName).toList();
        if (broNames.contains(deptDTO.getName())) {
            throw new ResourceConflictException("同级下存在相同名称的部门！");
        }
        department.setName(deptDTO.getName());
        if (deptDTO.getPosition() != null) {
            department.setPosition(deptDTO.getPosition());
        }
        department.setUpdateTime(LocalDateTime.now());
        departmentRepo.save(department);
        List<Position> positions = positionRepo.findAllByDeptId(id);
        List<Position> newPositions = positions.stream().peek(p -> {
            p.setDeptName(deptDTO.getName());
            p.setUpdateTime(LocalDateTime.now());
        }).toList();
        positionRepo.saveAll(newPositions);
        return convertToVO(department);
    }

    @Override
    @Transactional
    public void deleteDepartment(Long id) {
        if (id == 1L) {
            throw new NotAllowedException("不能删除顶级机构!");
        }
        departmentRepo.deleteById(id);
        positionRepo.deleteAllByDeptId(id);
    }

    private DepartmentVO convertToVO(Department department) {
        Set<DepartmentVO> children = department.getChildrenDepartments().stream()
                .map(this::convertToVO)
                .sorted(Comparator.comparing(DepartmentVO::getPosition))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return new DepartmentVO(department.getId(), department.getName(), department.getDescription(), department.getPosition(), children);
    }

    private Specification<Department> searchByQuery(String q) {
        return (Root<Department> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {
            if (q == null || q.isEmpty()) {
                return cb.conjunction(); // 如果查询参数为空，则返回所有结果
            }

            // 模糊匹配name和description
            String likePattern = "%" + q + "%";
            return cb.like(root.get("name"), likePattern);
            // 组合条件：name或description匹配
        };
    }

    // 递归查找部门的父部门，保留层级关系
    private void fetchAllDepartments(Department department, Set<Department> resultSet) {
        Department parentDepartment = department.getParentDepartment();
        if (parentDepartment != null) {
            resultSet.add(department.getParentDepartment());
            fetchAllDepartments(parentDepartment, resultSet);
        }
    }

    //组建部门树
    private Department buildDepartmentTree(Set<Department> resultSet) {
        Department topDepartment = null;
        List<Long> deptIds = new ArrayList<>();
        for (Department department : resultSet) {
            if (department.getParentDepartment() == null) {
                topDepartment = department;
            } else {
                deptIds.add(department.getId());
            }
        }
        if (topDepartment != null) {
            Set<Department> childrenDepartments = topDepartment.getChildrenDepartments();
            Set<Department> collect = childrenDepartments.stream()
                    .filter(d -> deptIds.contains(d.getId()))
                    .collect(Collectors.toSet());
            topDepartment.setChildrenDepartments(collect);
            filterDepartment(deptIds, collect);
        }
        return topDepartment;
    }

    private void filterDepartment(List<Long> deptIds, Set<Department> departments) {
        for (Department department : departments) {
            Set<Department> childrenDepartments = department.getChildrenDepartments();
            Set<Department> collect = childrenDepartments.stream()
                    .filter(d -> deptIds.contains(d.getId()))
                    .collect(Collectors.toSet());
            department.setChildrenDepartments(collect);
            if (collect.size() > 0) {
                filterDepartment(deptIds, collect);
            }
        }
    }
}
