package cn.xryder.base.repo.system;

import cn.xryder.base.domain.entity.system.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * @Author: joetao
 * @Date: 2024/9/19 16:09
 */
public interface PositionRepo extends JpaRepository<Position, Long>, JpaSpecificationExecutor<Position> {
    /**
     * 根据部门id查询所有职位
     *
     * @param deptId 部门id
     * @return 职位信息
     */
    List<Position> findAllByDeptId(Long deptId);

    /**
     * 根据部门id删除所有职位
     *
     * @param deptId 部门id
     */
    void deleteAllByDeptId(Long deptId);
}
