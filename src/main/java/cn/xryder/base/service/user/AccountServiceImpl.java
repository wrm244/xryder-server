package cn.xryder.base.service.user;

import cn.xryder.base.common.CommonUtil;
import cn.xryder.base.common.RsaUtil;
import cn.xryder.base.domain.dto.AccountDTO;
import cn.xryder.base.domain.entity.system.*;
import cn.xryder.base.domain.vo.RoleVO;
import cn.xryder.base.domain.vo.UserVO;
import cn.xryder.base.exception.custom.BadRequestException;
import cn.xryder.base.exception.custom.ResourceNotFoundException;
import cn.xryder.base.repo.AvatarRepo;
import cn.xryder.base.repo.system.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author: joetao
 * @Date: 2024/8/15 16:45
 */
@Service
public class AccountServiceImpl implements AccountService {
    private final UserRepo userRepo;
    private final AvatarRepo avatarRepo;
    private final UserRoleRepo userRoleRepo;
    private final RoleRepo roleRepo;
    private final PermissionRepo permissionRepo;
    private final PositionRepo positionRepo;
    private final RolePermissionRepo rolePermissionRepo;
    private final UserNotificationRepo userNotificationRepo;
    private final PasswordEncoder passwordEncoder;

    public AccountServiceImpl(UserRepo userRepo, AvatarRepo avatarRepo, UserRoleRepo userRoleRepo, RoleRepo roleRepo, PermissionRepo permissionRepo, PositionRepo positionRepo, RolePermissionRepo rolePermissionRepo, UserNotificationRepo userNotificationRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.avatarRepo = avatarRepo;
        this.userRoleRepo = userRoleRepo;
        this.roleRepo = roleRepo;
        this.permissionRepo = permissionRepo;
        this.positionRepo = positionRepo;
        this.rolePermissionRepo = rolePermissionRepo;
        this.userNotificationRepo = userNotificationRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public UserVO getAccountInfo(String username) {
        User user = userRepo.findById(username).orElseThrow(() -> new ResourceNotFoundException("用户不存在！"));
        Set<RoleVO> roles = getRoles(username);
        List<String> permissions = getPermissionsByRoles(roles);
        Long positionId = user.getPositionId();
        Integer unread = 0;
        Long unreadCount = userNotificationRepo.countByUsernameAndAndStatus(username, unread);
        UserVO userVO = UserVO.builder()
                .nickname(user.getNickname())
                .departmentId(user.getDepartmentId())
                .username(username)
                .avatar(user.getAvatar())
                .mobile(user.getMobile())
                .email(user.getEmail())
                .newMails(unreadCount)
                .loginDate(user.getLoginDate())
                .roles(roles)
                .permissions(permissions)
                .build();
        if (positionId != null) {
            Optional<Position> positionOptional = positionRepo.findById(positionId);
            // 删除职位也不影响此处的逻辑。所以删除职位代码没有处理用户表中的职位信息。
            positionOptional.ifPresent(position -> userVO.setPosition(position.getName()));
        }
        return userVO;
    }

    private List<String> getPermissionsByRoles(Set<RoleVO> roles) {
        List<Long> roleIds = roles.stream().map(RoleVO::getId).toList();
        List<RolePermission> rolePermissions = rolePermissionRepo.findAllByIdRoleIdIn(roleIds);
        List<Permission> permissions = permissionRepo.findAllByIdIn(rolePermissions.stream().map(rp -> rp.getId().getPermissionId()).collect(Collectors.toList()));
        return permissions.stream().map(Permission::getScope).toList();
    }

    private Set<RoleVO> getRoles(String username) {
        List<UserRole> userRoles = userRoleRepo.findAllByUsername(username);
        List<Role> roles = roleRepo.findAllById(userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toList()));
        return roles.stream().map(r -> {
            RoleVO roleVO = new RoleVO();
            roleVO.setId(r.getId());
            roleVO.setName(r.getName());
            return roleVO;
        }).collect(Collectors.toSet());
    }

    public Avatar saveAvatar(MultipartFile file, String username) throws IOException {
        String base64Encoded = Base64.getEncoder().encodeToString(file.getBytes());
        Avatar avatar = new Avatar();
        avatar.setFileName(file.getOriginalFilename());
        avatar.setFileData(base64Encoded);
        avatarRepo.save(avatar);
        User user = userRepo.findById(username).orElseThrow(() -> new ResourceNotFoundException("用户不存在！"));
        user.setAvatar(base64Encoded);
        userRepo.save(user);
        return avatar;
    }

    @Override
    public UserVO updateAccount(String username, AccountDTO account) {
        User user = userRepo.findById(username).orElseThrow(() -> new ResourceNotFoundException("用户不存在！"));
        user.setUpdateTime(LocalDateTime.now());
        user.setNickname(account.getNickname());
        user.setRemark(account.getRemark());
        user.setEmail(account.getEmail());
        user.setMobile(account.getMobile());
        userRepo.save(user);
        Set<RoleVO> roles = getRoles(username);
        List<String> permissions = getPermissionsByRoles(roles);
        return UserVO.builder()
                .nickname(user.getNickname())
                .departmentId(user.getDepartmentId())
                .username(username)
                .avatar(user.getAvatar())
                .mobile(user.getMobile())
                .email(user.getEmail())
                .loginDate(user.getLoginDate())
                .roles(roles)
                .permissions(permissions)
                .build();
    }

    @Override
    public void changePassword(String username, String oldPassword, String newPassword) throws Exception {
        User user = userRepo.findById(username).orElseThrow(() -> new ResourceNotFoundException("用户不存在！"));
        String oldDecryptPassword = RsaUtil.decryptWithPrivate(oldPassword);
        boolean matches = passwordEncoder.matches(oldDecryptPassword, user.getPassword());
        if (!matches) {
            throw new BadRequestException("密码错误！");
        }
        String newDecryptPassword = RsaUtil.decryptWithPrivate(newPassword);
        if (!CommonUtil.isValidPassword(newDecryptPassword)) {
            throw new BadRequestException("密码不符合要求！");
        }
        String newEncodedPassword = passwordEncoder.encode(newDecryptPassword);
        user.setPassword(newEncodedPassword);
        user.setUpdateTime(LocalDateTime.now());
        userRepo.save(user);
    }
}
