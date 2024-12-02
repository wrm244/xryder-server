package cn.xryder.base.domain.entity.system;

import jakarta.persistence.*;
import lombok.Data;

/**
 * @Author: joetao
 * @Date: 2024/9/24 16:23
 */
@Entity
@Table(name = "sys_user_notification")
@Data
public class UserNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "notification_id")
    private Notification notification;

    private String username;
    /**
     * 0：未读 1：已读
     */
    private Integer status;
}
