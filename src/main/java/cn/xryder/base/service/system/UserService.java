package cn.xryder.base.service.system;

import cn.xryder.base.domain.PageResult;
import cn.xryder.base.domain.dto.system.UserDTO;
import cn.xryder.base.domain.dto.system.UserRoleDTO;
import cn.xryder.base.domain.dto.system.UserSettingDTO;
import cn.xryder.base.domain.vo.UserVO;

import java.util.List;

/**
 * @Author: joetao
 * @Date: 2024/8/20 10:46
 */
public interface UserService {
    /**
     * 添加用户
     * @param user 用户信息
     * @param creator 创建者账号
     * @return 用户信息
     */
    UserVO addUser(UserDTO user, String creator);

    /**
     * 删除用户
     * @param username 用户名
     */
    void deleteUser(String username);

    UserVO getUserById(String username);

    /**
     * 查询用户
     * @param q 查询条件，用于模糊搜索用户名
     * @param type 0: 查询禁用， 1：查询启用，其他查询全部
     * @param deptId 部门id
     * @param page 第几页
     * @param pageSize 每页返回行数
     * @return 用户列表
     */
    PageResult<List<UserVO>> getUsers(String q, Integer type, Long deptId, int page, int pageSize);

    /**
     * 设置用户角色
     * @param userRole 用户角色信息
     * @param creator 创建者
     */
    void setUserRole(UserRoleDTO userRole, String creator);

    /**
     * 重置密码
     * @param username 用户名
     */
    void resetPwd(String username);

    /**
     * 启用/禁用用户
     * @param username 用户名
     */
    void toggleEnabled(String username);

    /**
     * 设置用户部门和职位
     * @param user 用户设置信息
     */
    void setUser(UserSettingDTO user);
}
