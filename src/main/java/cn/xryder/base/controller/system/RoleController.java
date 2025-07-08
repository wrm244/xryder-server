package cn.xryder.base.controller.system;

import cn.xryder.base.config.OperationLog;
import cn.xryder.base.domain.PageResult;
import cn.xryder.base.domain.R;
import cn.xryder.base.domain.dto.system.RoleDTO;
import cn.xryder.base.domain.vo.PermissionVO;
import cn.xryder.base.domain.vo.RoleVO;
import cn.xryder.base.service.system.RoleService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * @Author: joetao
 * @Date: 2024/8/20 10:46
 */
@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {
    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @OperationLog("添加角色")
    @PostMapping
    @PreAuthorize("hasAuthority('system')")
    public R<RoleVO> addRole(@Valid @RequestBody RoleDTO role, Principal principal) {
        RoleVO createdRole = roleService.addRole(role, principal.getName());
        return R.ok(createdRole);
    }

    @OperationLog("修改角色信息")
    @PutMapping
    @PreAuthorize("hasAuthority('system')")
    public R<RoleVO> updateRole(@Valid @RequestBody RoleDTO role, Principal principal) {
        RoleVO createdRole = roleService.updateRole(role, principal.getName());
        return R.ok(createdRole);
    }

    @GetMapping("/pageable")
    @PreAuthorize("hasAuthority('system')")
    public R<PageResult<List<RoleVO>>> getRolesPageable(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        if (page <= 1) {
            page = 1;
        }
        PageResult<List<RoleVO>> roles = roleService.queryRoles(q, page, pageSize);
        return R.ok(roles);
    }

    @GetMapping
    public R<List<RoleVO>> getRoles() {
        PageResult<List<RoleVO>> roles = roleService.queryRoles("", 1, 100);
        return R.ok(roles.getData());
    }

    @OperationLog("删除角色")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system')")
    public R<?> deleteRoleById(@PathVariable Long id) {
        roleService.deleteRoles(id);
        return R.ok();
    }

    @GetMapping("/permissions")
    @PreAuthorize("hasAuthority('system')")
    public R<List<PermissionVO>> getPermissions() {
        List<PermissionVO> permissions = roleService.getPermissions();
        return R.ok(permissions);
    }
}
