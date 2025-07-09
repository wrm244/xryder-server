package cn.xryder.base.repo.system;

import cn.xryder.base.domain.entity.system.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @Author: joetao
 * @Date: 2024/7/30 16:23
 */
@Repository
public interface RoleRepo extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role> {

    /**
     * 根据角色名称查找角色
     *
     * @param name 角色名称
     * @return 角色信息
     */
    Optional<Role> findByName(String name);

    /**
     * 根据角色名称和类型查找角色
     *
     * @param name 角色名称
     * @param type 角色类型
     * @return 角色信息
     */
    Optional<Role> findByNameAndType(String name, Integer type);
}
