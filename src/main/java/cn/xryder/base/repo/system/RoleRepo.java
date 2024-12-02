package cn.xryder.base.repo.system;

import cn.xryder.base.domain.entity.system.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * @Author: joetao
 * @Date: 2024/7/30 16:23
 */
@Repository
public interface RoleRepo extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role> {
}
