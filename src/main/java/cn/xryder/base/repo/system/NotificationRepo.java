package cn.xryder.base.repo.system;

import cn.xryder.base.domain.entity.system.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @Author: joetao
 * @Date: 2024/9/25 10:01
 */
public interface NotificationRepo extends JpaRepository<Notification, Long> {
    Page<Notification> findByTitleContaining(String q, Pageable pageable);

    Page<Notification> findByTitleContainingAndCreatorEquals(String q, String creator, Pageable pageable);

    Page<Notification> findByCreatorEquals(String creator, Pageable pageable);
}
