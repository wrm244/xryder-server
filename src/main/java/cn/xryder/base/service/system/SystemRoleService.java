package cn.xryder.base.service.system;

import cn.xryder.base.common.enums.RoleTypeEnum;
import cn.xryder.base.common.enums.SystemRoleEnum;
import cn.xryder.base.domain.entity.system.Role;
import cn.xryder.base.repo.system.RoleRepo;
import org.springframework.stereotype.Service;

/**
 * 系统角色服务，用于获取系统内置角色的ID
 *
 * @Author: joetao
 * @Date: 2024/7/30 16:20
 */
@Service
public class SystemRoleService {

    private final RoleRepo roleRepo;

    public SystemRoleService(RoleRepo roleRepo) {
        this.roleRepo = roleRepo;
    }

    /**
     * 获取管理员角色ID
     *
     * @return 管理员角色ID
     */
    public Long getAdminRoleId() {
        return roleRepo.findByNameAndType(SystemRoleEnum.ADMIN.getName(), RoleTypeEnum.SYSTEM.getType())
                .map(Role::getId)
                .orElseThrow(() -> new RuntimeException("管理员角色不存在"));
    }

    /**
     * 获取普通用户角色ID
     *
     * @return 普通用户角色ID
     */
    public Long getUserRoleId() {
        return roleRepo.findByNameAndType(SystemRoleEnum.USER.getName(), RoleTypeEnum.SYSTEM.getType())
                .map(Role::getId)
                .orElseThrow(() -> new RuntimeException("普通用户角色不存在"));
    }
}
