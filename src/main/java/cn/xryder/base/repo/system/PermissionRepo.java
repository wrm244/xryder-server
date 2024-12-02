package cn.xryder.base.repo.system;

import cn.xryder.base.domain.entity.system.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * @Author: joetao
 * @Date: 2024/8/20 9:53
 */
public interface PermissionRepo extends JpaRepository<Permission, Long>, JpaSpecificationExecutor<Permission> {
    List<Permission> findAllByIdIn(List<Long> ids);
}
