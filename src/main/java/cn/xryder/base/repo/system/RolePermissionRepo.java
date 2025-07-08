package cn.xryder.base.repo.system;

import cn.xryder.base.domain.entity.system.RolePermission;
import cn.xryder.base.domain.entity.system.RolePermissionKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * @Author: joetao
 * @Date: 2024/8/20 9:48
 */
public interface RolePermissionRepo extends JpaRepository<RolePermission, RolePermissionKey>, JpaSpecificationExecutor<RolePermission> {
    /**
     * 根据角色id列表查询权限id列表
     *
     * @param roleIds 角色id列表
     * @return 权限id列表
     */
    List<RolePermission> findAllByIdRoleIdIn(List<Long> roleIds);

    /**
     * 根据角色id查询权限id列表
     *
     * @param roleId 角色id
     * @return 权限id列表
     */
    List<RolePermission> findAllByIdRoleId(Long roleId);

    /**
     * 删除角色权限信息
     *
     * @param roleId 角色id
     */
    void deleteAllByIdRoleId(Long roleId);
}
