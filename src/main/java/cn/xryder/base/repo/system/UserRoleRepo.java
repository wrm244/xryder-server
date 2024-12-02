package cn.xryder.base.repo.system;

import cn.xryder.base.domain.entity.system.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @Author: joetao
 * @Date: 2024/8/12 16:37
 */
@Repository
public interface UserRoleRepo extends JpaRepository<UserRole, Long>, JpaSpecificationExecutor<UserRole> {
    List<UserRole> findAllByUsername(String username);

    /**
     * 根据用户账号删除用户角色信息
     * @param username 用户账号
     */
    void deleteAllByUsername(String username);

    /**
     * 根据角色id删除用户角色关系
     * @param roleId 角色id
     */
    void deleteAllByRoleId(Long roleId);
}
