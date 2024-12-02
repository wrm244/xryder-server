package cn.xryder.base.service.system;

import cn.xryder.base.domain.PageResult;
import cn.xryder.base.domain.dto.system.PositionDTO;
import cn.xryder.base.domain.entity.system.Position;
import cn.xryder.base.domain.vo.PositionVO;
import cn.xryder.base.exception.custom.ResourceNotFoundException;
import cn.xryder.base.repo.system.PositionRepo;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: joetao
 * @Date: 2024/9/19 14:29
 */
@Service
public class PositionServiceImpl implements PositionService {
    private final PositionRepo positionRepo;

    public PositionServiceImpl(PositionRepo positionRepo) {
        this.positionRepo = positionRepo;
    }

    @Override
    public PositionVO addPosition(PositionDTO position, String username) {
        Position positionDO = new Position();
        BeanUtils.copyProperties(position, positionDO);
        positionDO.setCreateTime(LocalDateTime.now());
        positionDO.setUpdateTime(LocalDateTime.now());
        positionDO.setCreator(username);
        positionRepo.save(positionDO);
        PositionVO positionVO = new PositionVO();
        BeanUtils.copyProperties(positionDO, positionVO);
        return positionVO;
    }

    @Override
    public PageResult<List<PositionVO>> queryPositions(String q, Long deptId, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "updateTime"));
        Specification<Position> positionSpecification = searchByCriteria(q, deptId);
        Page<Position> all = positionRepo.findAll(positionSpecification, pageable);
        List<PositionVO> positions = new ArrayList<>();
        all.get().forEach(position -> {
            PositionVO positionVO = new PositionVO();
            BeanUtils.copyProperties(position, positionVO);
            positions.add(positionVO);
        });
        return PageResult.<List<PositionVO>>builder().page(page).data(positions).rows(positions.size()).total(all.getTotalElements()).build();

    }

    @Override
    public PositionVO updatePosition(PositionDTO position) {
        Long id = position.getId();
        Position positionDO = positionRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("职位不存在！"));
        BeanUtils.copyProperties(position, positionDO);
        positionDO.setUpdateTime(LocalDateTime.now());
        positionRepo.save(positionDO);
        PositionVO positionVO = new PositionVO();
        BeanUtils.copyProperties(positionDO, positionVO);
        return positionVO;
    }

    @Override
    public void deletePosition(Long id) {
        positionRepo.deleteById(id);
    }

    private Specification<Position> searchByCriteria(String q, Long deptId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 添加模糊查询条件
            if (q != null && !q.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("name"), "%" + q + "%"));
            }

            // 添加 departmentId 条件
            if (deptId != null) {
                predicates.add(criteriaBuilder.equal(root.get("deptId"), deptId));
            }

            // 构建最终的查询条件
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
