package cn.xryder.base.service.system;

import cn.xryder.base.common.RoleTypeEnum;
import cn.xryder.base.domain.PageResult;
import cn.xryder.base.domain.dto.system.RoleDTO;
import cn.xryder.base.domain.entity.system.Role;
import cn.xryder.base.domain.entity.system.RolePermission;
import cn.xryder.base.domain.entity.system.RolePermissionKey;
import cn.xryder.base.domain.vo.PermissionVO;
import cn.xryder.base.domain.vo.RoleVO;
import cn.xryder.base.exception.custom.BadRequestException;
import cn.xryder.base.exception.custom.ResourceNotFoundException;
import cn.xryder.base.repo.system.PermissionRepo;
import cn.xryder.base.repo.system.RolePermissionRepo;
import cn.xryder.base.repo.system.RoleRepo;
import cn.xryder.base.repo.system.UserRoleRepo;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author: joetao
 * @Date: 2024/8/20 16:07
 */
@Service
public class RoleServiceImpl implements RoleService {
    private final RoleRepo roleRepo;
    private final UserRoleRepo userRoleRepo;
    private final PermissionRepo permissionRepo;
    private final RolePermissionRepo rolePermissionRepo;

    public RoleServiceImpl(RoleRepo roleRepo, UserRoleRepo userRoleRepo, PermissionRepo permissionRepo, RolePermissionRepo rolePermissionRepo) {
        this.roleRepo = roleRepo;
        this.userRoleRepo = userRoleRepo;
        this.permissionRepo = permissionRepo;
        this.rolePermissionRepo = rolePermissionRepo;
    }

    @Override
    public RoleVO addRole(RoleDTO role, String username) {
        String roleName = role.getName();
        if (roleName.contains("管理")) {
            throw new BadRequestException("存在非法字符！");
        }
        Role roleDO = new Role();
        BeanUtils.copyProperties(role, roleDO);
        roleDO.setType(RoleTypeEnum.CUSTOM.getType());
        roleDO.setCreateTime(LocalDateTime.now());
        roleDO.setUpdateTime(LocalDateTime.now());
        roleDO.setCreator(username);
        roleRepo.save(roleDO);

        List<RolePermission> rolePermissions = getRolePermissions(role, username, roleDO.getId());

        rolePermissionRepo.saveAll(rolePermissions);

        return RoleVO.builder()
                .id(roleDO.getId())
                .name(roleDO.getName())
                .createTime(roleDO.getCreateTime())
                .level(roleDO.getLevel())
                .remark(roleDO.getRemark())
                .type(roleDO.getType())
                .build();
    }

    @Override
    public PageResult<List<RoleVO>> queryRoles(String q, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "updateTime"));
        Specification<Role> spec = searchByCriteria(q);
        Page<Role> all = roleRepo.findAll(spec, pageable);
        List<RoleVO> roles = new ArrayList<>();
        all.get().forEach(r -> {
            RoleVO roleVO = new RoleVO();
            Long roleId = r.getId();
            BeanUtils.copyProperties(r, roleVO);
            List<Long> permissionIds = rolePermissionRepo.findAllByIdRoleId(roleId).stream().map(rp -> rp.getId().getPermissionId()).toList();
            roleVO.setPermissions(permissionIds);
            roles.add(roleVO);
        });
        return PageResult.<List<RoleVO>>builder().page(page).data(roles).rows(roles.size()).total(all.getTotalElements()).build();
    }

    @Override
    public List<PermissionVO> getPermissions() {
        return permissionRepo.findAll().stream().map(permission -> {
            PermissionVO permissionVO = new PermissionVO();
            permissionVO.setId(permission.getId());
            permissionVO.setName(permission.getName());
            return permissionVO;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteRoles(Long id) {
        Role role = roleRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("角色不存在！"));
        if (Objects.equals(role.getType(), RoleTypeEnum.SYSTEM.getType())) {
            throw new BadRequestException("无法删除系统内置角色！");
        }
        rolePermissionRepo.deleteAllByIdRoleId(id);
        userRoleRepo.deleteAllByRoleId(id);
        roleRepo.delete(role);
    }

    @Override
    @Transactional
    public RoleVO updateRole(RoleDTO role, String username) {
        Role roleDO = roleRepo.findById(role.getId())
                .orElseThrow(() -> new ResourceNotFoundException("角色不存在！"));
        BeanUtils.copyProperties(role, roleDO);
        roleDO.setUpdateTime(LocalDateTime.now());
        roleRepo.save(roleDO);

        //删除原有角色权限信息
        rolePermissionRepo.deleteAllByIdRoleId(role.getId());
        //插入新的角色权限信息
        List<RolePermission> rolePermissions = getRolePermissions(role, username, role.getId());
        rolePermissionRepo.saveAll(rolePermissions);

        return RoleVO.builder()
                .id(roleDO.getId())
                .name(roleDO.getName())
                .createTime(roleDO.getCreateTime())
                .level(roleDO.getLevel())
                .remark(roleDO.getRemark())
                .type(roleDO.getType())
                .build();
    }

    private List<RolePermission> getRolePermissions(RoleDTO role, String username, Long roleId) {
        Long[] permissions = role.getPermissions();
        return Arrays.stream(permissions).map(p -> {
            RolePermissionKey rolePermissionKey = new RolePermissionKey();
            RolePermission rolePermission = new RolePermission();
            rolePermissionKey.setRoleId(roleId);
            rolePermissionKey.setPermissionId(p);
            rolePermission.setId(rolePermissionKey);
            rolePermission.setCreator(username);
            rolePermission.setCreateTime(LocalDateTime.now());
            rolePermission.setUpdateTime(LocalDateTime.now());
            return rolePermission;
        }).toList();
    }

    private Specification<Role> searchByCriteria(String q) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.notEqual(root.get("name"), "管理员"));
            // 添加模糊查询条件
            if (q != null && !q.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("name"), "%" + q + "%"));
            }

            // 构建最终的查询条件
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
