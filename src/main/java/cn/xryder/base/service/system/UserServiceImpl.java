package cn.xryder.base.service.system;

import cn.xryder.base.common.Admin;
import cn.xryder.base.common.CommonStatusEnum;
import cn.xryder.base.common.SystemRoleEnum;
import cn.xryder.base.domain.PageResult;
import cn.xryder.base.domain.dto.system.UserDTO;
import cn.xryder.base.domain.dto.system.UserRoleDTO;
import cn.xryder.base.domain.dto.system.UserSettingDTO;
import cn.xryder.base.domain.entity.system.Position;
import cn.xryder.base.domain.entity.system.Role;
import cn.xryder.base.domain.entity.system.User;
import cn.xryder.base.domain.entity.system.UserRole;
import cn.xryder.base.domain.vo.RoleVO;
import cn.xryder.base.domain.vo.UserVO;
import cn.xryder.base.exception.custom.BadRequestException;
import cn.xryder.base.exception.custom.ResourceConflictException;
import cn.xryder.base.exception.custom.ResourceNotFoundException;
import cn.xryder.base.repo.system.PositionRepo;
import cn.xryder.base.repo.system.RoleRepo;
import cn.xryder.base.repo.system.UserRepo;
import cn.xryder.base.repo.system.UserRoleRepo;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: joetao
 * @Date: 2024/8/12 14:14
 */

@Service
public class UserServiceImpl implements UserService {
    private final UserRepo userRepo;
    private final UserRoleRepo userRoleRepo;
    private final RoleRepo roleRepo;
    private final PositionRepo positionRepo;
    private final PasswordEncoder passwordEncoder;
    private static final String DEFAULT_PWD = "Jx1016!";

    public UserServiceImpl(UserRepo userRepo, UserRoleRepo userRoleRepo, RoleRepo roleRepo, PositionRepo positionRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.userRoleRepo = userRoleRepo;
        this.roleRepo = roleRepo;
        this.positionRepo = positionRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public UserVO addUser(UserDTO user, String creator) {
        String username = user.getUsername();
        boolean exists = userRepo.existsById(username);
        if (exists) {
            throw new ResourceConflictException("账号已存在！");
        }
        User userDO = new User();
        BeanUtils.copyProperties(user, userDO);
        userDO.setEnabled(true);
        userDO.setPassword(passwordEncoder.encode(DEFAULT_PWD));
        String DEFAULT_AVATAR_BASE64 = "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";
        userDO.setAvatar(DEFAULT_AVATAR_BASE64);
        userDO.setCreateTime(LocalDateTime.now());
        userDO.setUpdateTime(LocalDateTime.now());
        userDO.setCreator(creator);
        userRepo.save(userDO);

        UserRole userRole = new UserRole();
        userRole.setRoleId(SystemRoleEnum.USER.getId());
        userRole.setUsername(username);
        userRoleRepo.save(userRole);

        RoleVO roleVO = new RoleVO();
        roleVO.setName(SystemRoleEnum.USER.getName());
        roleVO.setId(SystemRoleEnum.USER.getId());
        return UserVO.builder()
                .username(username)
                .nickname(user.getNickname())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .roles(Set.of(roleVO))
                .build();
    }

    @Transactional
    public void deleteUser(String username) {
        userRepo.deleteById(username);
        userRoleRepo.deleteAllByUsername(username);
    }

    public UserVO getUserById(String username) {
        User user = userRepo.findById(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在！"));
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        List<UserRole> userRoles = userRoleRepo.findAllByUsername(username);
        List<Role> roles = roleRepo.findAllById(userRoles.stream().map(UserRole::getRoleId).collect(Collectors.toList()));
        Set<RoleVO> roleVOS = roles.stream().map(r -> {
            RoleVO roleVO = new RoleVO();
            roleVO.setId(r.getId());
            roleVO.setName(r.getName());
            return roleVO;
        }).collect(Collectors.toSet());
        userVO.setRoles(roleVOS);
        Long positionId = user.getPositionId();
        if (positionId != null) {
            Optional<Position> positionOptional = positionRepo.findById(positionId);
            // 删除职位也不影响此处的逻辑。所以删除职位代码没有处理用户表中的职位信息。
            positionOptional.ifPresent(position -> userVO.setPosition(position.getName()));
        }
        return userVO;
    }

    public PageResult<List<UserVO>> getUsers(String q, Integer type, Long deptId, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "updateTime"));
        Specification<User> spec = searchByCriteria(q, type, deptId);
        Page<User> all = userRepo.findAll(spec, pageable);
        List<UserVO> users = new ArrayList<>();
        all.get().forEach(u -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(u, userVO);
            users.add(userVO);
        });
        return PageResult.<List<UserVO>>builder().page(page).data(users).rows(users.size()).total(all.getTotalElements()).build();
    }

    @Override
    @Transactional
    public void setUserRole(UserRoleDTO userRole, String creator) {
        String username = userRole.getUsername();
        Long[] roles = userRole.getRoles();
        Set<Long> roleSet = new HashSet<>(Arrays.asList(roles));
        boolean userExists = userRepo.existsById(username);
        if (!userExists) {
            throw new ResourceNotFoundException("用户不存在！");
        }
        if (roleSet.size() == 0) {
            throw new BadRequestException("用户必须设置一个角色");
        }
        //如果修改的是admin账号，保证该账号有管理员角色
        if (username.equals(Admin.username)) {
            roleSet.add(SystemRoleEnum.ADMIN.getId());
        }
        userRoleRepo.deleteAllByUsername(username);
        List<UserRole> userRoleDOList = roleSet.stream().map(r -> {
            UserRole userRoleDO = new UserRole();
            userRoleDO.setRoleId(r);
            userRoleDO.setUsername(username);
            userRoleDO.setCreator(creator);
            userRoleDO.setCreateTime(LocalDateTime.now());
            userRoleDO.setUpdateTime(LocalDateTime.now());
            return userRoleDO;
        }).toList();
        userRoleRepo.saveAll(userRoleDOList);
    }

    @Override
    public void resetPwd(String username) {
        User user = userRepo.findById(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在！"));
        user.setPassword(passwordEncoder.encode(DEFAULT_PWD));
        user.setUpdateTime(LocalDateTime.now());
        userRepo.save(user);
    }

    @Override
    public void toggleEnabled(String username) {
        User user = userRepo.findById(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在！"));
        user.toggleEnabled();
        user.setUpdateTime(LocalDateTime.now());
        userRepo.save(user);
    }

    @Override
    public void setUser(UserSettingDTO user) {
        String username = user.getUsername();
        User userDO = userRepo.findById(username)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在！"));
        userDO.setDepartmentId(user.getDeptId());
        userDO.setPositionId(user.getPositionId());
        userDO.setUpdateTime(LocalDateTime.now());
        userRepo.save(userDO);
    }

    private Specification<User> searchByCriteria(String nickname, Integer type, Long departmentId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.notEqual(root.get("username"), "admin"));
            // 添加模糊查询条件 nickname
            if (nickname != null && !nickname.isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("nickname"), "%" + nickname + "%"));
            }

            // 添加启用/禁用条件
            if (Objects.equals(CommonStatusEnum.ENABLE.getStatus(), type)) {
                predicates.add(criteriaBuilder.equal(root.get("enabled"), true));
            } else if (Objects.equals(CommonStatusEnum.DISABLE.getStatus(), type)) {
                predicates.add(criteriaBuilder.equal(root.get("enabled"), false));
            }

            // 添加 departmentId 条件
            if (departmentId != null) {
                predicates.add(criteriaBuilder.equal(root.get("departmentId"), departmentId));
            }

            // 构建最终的查询条件
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

