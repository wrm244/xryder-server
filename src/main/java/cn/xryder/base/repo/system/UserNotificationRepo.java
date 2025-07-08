package cn.xryder.base.repo.system;

import cn.xryder.base.domain.entity.system.UserNotification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @Author: joetao
 * @Date: 2024/9/25 10:01
 */
public interface UserNotificationRepo extends JpaRepository<UserNotification, Long> {
    /**
     * 统计用户未读通知数量
     *
     * @param username 用户账号
     * @param status   未读状态
     * @return 未读数量
     */
    Long countByUsernameAndAndStatus(String username, Integer status);

    @Query("SELECT u from UserNotification u  LEFT JOIN u.notification n  where u.username = :username and u.status = 0 ORDER BY u.id DESC")
    List<UserNotification> findTop100UnreadByUsername(@Param("username") String username, Pageable pageable);

    @Query("SELECT u from UserNotification u  LEFT JOIN u.notification n  where u.username = :username ORDER BY u.id DESC")
    List<UserNotification> findTop100ByUsername(@Param("username") String username, Pageable pageable);

    UserNotification findUserNotificationByNotificationIdAndUsername(Long id, String username);
}
