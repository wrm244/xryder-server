package cn.xryder.base.service;

import cn.xryder.base.domain.LoginUser;
import cn.xryder.base.domain.entity.system.*;
import cn.xryder.base.repo.system.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author: joetao
 * @Date: 2024/7/31 9:39
 */
@Component
@Slf4j
public class CustomUserDetailService implements UserDetailsService {
    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final UserRoleRepo userRoleRepo;
    private final RolePermissionRepo rolePermissionRepo;
    private final PermissionRepo permissionRepo;
    private final LoginAttemptService loginAttemptService;

    public CustomUserDetailService(UserRepo userRepo, RoleRepo roleRepo, UserRoleRepo userRoleRepo, RolePermissionRepo rolePermissionRepo, PermissionRepo permissionRepo, LoginAttemptService loginAttemptService) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.userRoleRepo = userRoleRepo;
        this.rolePermissionRepo = rolePermissionRepo;
        this.permissionRepo = permissionRepo;
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (loginAttemptService.isBlocked(username)) {
            log.warn("账号{}被锁定", username);
            throw new LockedException("失败次数过多，账号已被锁定！请稍后再试。");
        }
        Optional<User> userOptional = userRepo.findById(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            List<UserRole> userRoles = userRoleRepo.findAllByUsername(username);
            List<Role> roles = roleRepo.findAllById(userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toList()));
            List<RolePermission> rolePermissions = rolePermissionRepo.findAllByIdRoleIdIn(roles.stream().map(Role::getId).collect(Collectors.toList()));
            List<Permission> permissions = permissionRepo.findAllByIdIn(rolePermissions.stream().map(rp -> rp.getId().getPermissionId()).collect(Collectors.toList()));
            Set<GrantedAuthority> authorities = permissions.stream().map(p -> new SimpleGrantedAuthority(p.getScope())).collect(Collectors.toSet());
            if (!user.isEnabled()) {
                throw new DisabledException("用户账户已被禁用！");
            }
            return LoginUser.builder()
                    .username(username)
                    .name(user.getNickname())
                    .password(user.getPassword())
                    .authorities(authorities)
                    .enabled(user.isEnabled())
                    .build();
        }
        // 这里即使抛出了UsernameNotFoundException，但是在登录失败异常处理中也不会打印这个异常错误信息
        // 原因是：spring官方为了保证安全，不会对于错误做更详细的提示。捕获了该异常后，会统一抛出BadCredentialsException。
        throw new UsernameNotFoundException("用户名或密码错误！");
    }


}
