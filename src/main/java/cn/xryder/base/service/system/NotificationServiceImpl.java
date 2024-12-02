package cn.xryder.base.service.system;

import cn.xryder.base.domain.PageResult;
import cn.xryder.base.domain.dto.system.NotificationDTO;
import cn.xryder.base.domain.entity.system.Department;
import cn.xryder.base.domain.entity.system.Notification;
import cn.xryder.base.domain.entity.system.User;
import cn.xryder.base.domain.entity.system.UserNotification;
import cn.xryder.base.domain.vo.NotificationVO;
import cn.xryder.base.repo.system.DepartmentRepo;
import cn.xryder.base.repo.system.NotificationRepo;
import cn.xryder.base.repo.system.UserNotificationRepo;
import cn.xryder.base.repo.system.UserRepo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author: joetao
 * @Date: 2024/9/25 9:45
 */
@Service
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepo notificationRepo;
    private final UserNotificationRepo userNotificationRepo;
    private final UserRepo userRepo;
    private final DepartmentRepo departmentRepo;
    public NotificationServiceImpl(NotificationRepo notificationRepo, UserNotificationRepo userNotificationRepo, UserRepo userRepo, DepartmentRepo departmentRepo) {
        this.notificationRepo = notificationRepo;
        this.userNotificationRepo = userNotificationRepo;
        this.userRepo = userRepo;
        this.departmentRepo = departmentRepo;
    }

    @Override
    public void send(NotificationDTO notificationDTO, String username) {
        Notification notification = new Notification();
        BeanUtils.copyProperties(notificationDTO, notification);
        notification.setCreator(username);
        notification.setCreateTime(LocalDateTime.now());
        notification.setUpdateTime(LocalDateTime.now());
        notificationRepo.save(notification);
        Integer type = notificationDTO.getType();
        int departmentType = 2;
        int unread = 0;
        List<UserNotification> userNotifications = new ArrayList<>();
        Set<String> usernames;
        if (departmentType == type) {
            List<Long> deptIds = notificationDTO.getDeptIds();
            Set<Long> childrenDeptIds = getChildrenDeptIds(deptIds);
            deptIds.addAll(childrenDeptIds);
            List<User> users = userRepo.findAllByDepartmentIdIn(deptIds);
            usernames = users.stream().map(User::getUsername).collect(Collectors.toSet());
        } else {
            usernames = userRepo.findAll().stream().map(User::getUsername).collect(Collectors.toSet());
        }
        for (String name: usernames) {
            UserNotification userNotification = new UserNotification();
            userNotification.setNotification(notification);
            userNotification.setUsername(name);
            userNotification.setStatus(unread);
            userNotifications.add(userNotification);
        }
        userNotificationRepo.saveAll(userNotifications);
    }

    @Override
    public PageResult<List<NotificationVO>> getNotifications(String q, int page, int pageSize, String username) {
        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by(Sort.Direction.DESC, "id"));
        Page<Notification> all;
        if (StringUtils.isEmpty(q)) {
            all = notificationRepo.findByCreatorEquals(username, pageable);
        } else {
            all = notificationRepo.findByTitleContainingAndCreatorEquals(q, username, pageable);
        }
        List<NotificationVO> notifications = new ArrayList<>();
        all.get().forEach(notice -> {
            NotificationVO notificationVO = new NotificationVO();
            BeanUtils.copyProperties(notice, notificationVO);
            notifications.add(notificationVO);
        });
        return PageResult.<List<NotificationVO>>builder().page(page).data(notifications).rows(notifications.size()).total(all.getTotalElements()).build();

    }

    private Set<Long> getChildrenDeptIds(List<Long> deptIds) {
        Set<Long> allIds = new HashSet<>();
        for (Long deptId: deptIds) {
            Set<Long> ids = getAllChildrenDepartmentIds(deptId);
            allIds.addAll(ids);
        }
        return allIds;
    }


    // 获取指定部门的所有子部门 ID
    public Set<Long> getAllChildrenDepartmentIds(Long departmentId) {
        // 通过 id 获取部门实体
        Department department = departmentRepo.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found, deptId：" + departmentId));

        // 初始化一个 Set 来保存所有子部门的 ID
        Set<Long> departmentIds = new HashSet<>();
        // 递归获取所有子部门
        collectChildrenDepartmentIds(department, departmentIds);

        return departmentIds;
    }

    // 递归方法，用于收集所有子部门及其子部门的 ID
    private void collectChildrenDepartmentIds(Department department, Set<Long> departmentIds) {
        // 遍历当前部门的所有子部门
        for (Department child : department.getChildrenDepartments()) {
            // 添加子部门 ID 到集合
            departmentIds.add(child.getId());
            // 递归调用，收集子部门的子部门
            collectChildrenDepartmentIds(child, departmentIds);
        }
    }
}
