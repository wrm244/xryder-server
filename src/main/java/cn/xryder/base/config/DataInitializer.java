package cn.xryder.base.config;

import cn.xryder.base.common.Admin;
import cn.xryder.base.common.RoleTypeEnum;
import cn.xryder.base.common.SystemRoleEnum;
import cn.xryder.base.domain.entity.system.*;
import cn.xryder.base.repo.system.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

/**
 * @Author: joetao
 * @Date: 2024/7/30 16:20
 */
@Configuration
public class DataInitializer {
    @Bean
    public CommandLineRunner loadData(UserRepo userRepository, RoleRepo roleRepo, UserRoleRepo userRoleRepo, RolePermissionRepo rolePermissionRepo, DepartmentRepo departmentRepo, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findById(Admin.username).isEmpty()) {
                User admin = new User();
                admin.setUsername(Admin.username);
                admin.setNickname(Admin.nickname);
                admin.setPassword(passwordEncoder.encode(Admin.password)); // 使用密码编码器加密密码
                admin.setEnabled(true);
                userRepository.save(admin);
                System.out.println(" admin password is: " + Admin.password);
                // 角色1
                Role role = new Role();
                role.setId(SystemRoleEnum.ADMIN.getId());
                role.setName(SystemRoleEnum.ADMIN.getName());
                role.setType(RoleTypeEnum.SYSTEM.getType());
                roleRepo.save(role);

                // 角色2
                Role role2 = new Role();
                role2.setId(SystemRoleEnum.USER.getId());
                role2.setName(SystemRoleEnum.USER.getName());
                role2.setType(RoleTypeEnum.SYSTEM.getType());
                roleRepo.save(role);

                // 初始化默认用户的管理员角色
                UserRole userRole = new UserRole();
                userRole.setRoleId(SystemRoleEnum.ADMIN.getId());
                userRole.setUsername(Admin.username);
                userRoleRepo.save(userRole);

                // 初始化角色权限信息
                RolePermissionKey rolePermissionKey = new RolePermissionKey();
                RolePermission rolePermission = new RolePermission();
                rolePermissionKey.setRoleId(1L);
                rolePermissionKey.setPermissionId(1L);
                rolePermission.setId(rolePermissionKey);
                rolePermission.setCreator(Admin.username);
                rolePermission.setCreateTime(LocalDateTime.now());
                rolePermission.setUpdateTime(LocalDateTime.now());
                rolePermissionRepo.save(rolePermission);
            }
            if (departmentRepo.findById(1L).isEmpty()) {
                // 初始化顶级部门
                Department department = new Department();
                department.setCreator(Admin.username);
                department.setPosition(1);
                department.setCreateTime(LocalDateTime.now());
                department.setName("组织");
                department.setDescription("顶级机构");
                departmentRepo.save(department);
            }

        };
    }
}
