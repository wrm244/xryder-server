package cn.xryder.base.service.system;

import cn.xryder.base.domain.PageResult;
import cn.xryder.base.domain.dto.system.RoleDTO;
import cn.xryder.base.domain.vo.PermissionVO;
import cn.xryder.base.domain.vo.RoleVO;

import java.util.List;

/**
 * 角色管理
 * @Author: joetao
 * @Date: 2024/8/20 15:20
 */
public interface RoleService {
    RoleVO addRole(RoleDTO role, String username);

    /**
     * 查询角色
     * @param q 搜索条件
     * @param page 第几页
     * @param pageSize 返回条数
     * @return 角色信息
     */
    PageResult<List<RoleVO>> queryRoles(String q, int page, int pageSize);

    /**
     * 获取权限
     * @return 权限列表
     */
    List<PermissionVO> getPermissions();

    /**
     * 删除角色
     * @param id 角色id
     */
    void deleteRoles(Long id);

    /**
     * 更新角色
     * @param role 角色信息
     * @param username 当前用户账号
     * @return 角色信息
     */
    RoleVO updateRole(RoleDTO role, String username);
}
