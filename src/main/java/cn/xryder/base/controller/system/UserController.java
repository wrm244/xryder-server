package cn.xryder.base.controller.system;

import cn.xryder.base.common.Admin;
import cn.xryder.base.config.OperationLog;
import cn.xryder.base.domain.PageResult;
import cn.xryder.base.domain.ResultJson;
import cn.xryder.base.domain.dto.system.UserDTO;
import cn.xryder.base.domain.dto.system.UserRoleDTO;
import cn.xryder.base.domain.dto.system.UserSettingDTO;
import cn.xryder.base.domain.vo.UserVO;
import cn.xryder.base.exception.custom.BadRequestException;
import cn.xryder.base.service.system.UserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * @Author: joetao
 * @Date: 2024/8/12 14:13
 */
@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @OperationLog("添加用户")
    @PostMapping
    @PreAuthorize("hasAuthority('system')")
    public ResultJson<UserVO> addUser(@Valid  @RequestBody UserDTO user, Principal principal) {
        UserVO createdUser = userService.addUser(user, principal.getName());
        return ResultJson.ok(createdUser);
    }

    @OperationLog("设置用户部门及职位")
    @PutMapping("/setting")
    @PreAuthorize("hasAuthority('system')")
    public ResultJson<?> setUser(@RequestBody UserSettingDTO user) {
        userService.setUser(user);
        return ResultJson.ok();
    }

    @OperationLog("删除用户")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system')")
    public ResultJson<Object> deleteUser(@PathVariable String id, Principal principal) {
        if (principal.getName().equals(id)) {
            throw new BadRequestException("无法删除当前登录账号！");
        }
        userService.deleteUser(id);
        return ResultJson.ok();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system')")
    public ResultJson<UserVO> getUserById(@PathVariable String id) {
        UserVO user = userService.getUserById(id);
        return ResultJson.ok(user);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('system')")
    public ResultJson<PageResult<List<UserVO>>> getUsers(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Long deptId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        if (page <= 1) {
            page = 1;
        }
        PageResult<List<UserVO>> users = userService.getUsers(q, type, deptId, page, pageSize);
        return ResultJson.ok(users);
    }

    @OperationLog("分配用户角色")
    @PostMapping("/roles")
    @PreAuthorize("hasAuthority('system')")
    public ResultJson<?> setUserRole(@RequestBody UserRoleDTO userRole, Principal principal) {
        userService.setUserRole(userRole, principal.getName());
        return ResultJson.ok();
    }

    @OperationLog("重置用户密码")
    @PutMapping("/{username}/pwd/reset")
    @PreAuthorize("hasAuthority('system')")
    public ResultJson<?> resetPwd(@PathVariable String username) {
        if (Admin.username.equals(username)) {
            throw new BadRequestException("不能操作超管账号！");
        }
        userService.resetPwd(username);
        return ResultJson.ok();
    }

    @OperationLog("启用/禁用用户")
    @PutMapping("/{username}/status")
    @PreAuthorize("hasAuthority('system')")
    public ResultJson<?> toggleEnabled(@PathVariable String username, Principal principal) {
        if (principal.getName().equals(username)) {
            throw new BadRequestException("不能禁用当前登录账号！");
        }
        userService.toggleEnabled(username);
        return ResultJson.ok();
    }

}

