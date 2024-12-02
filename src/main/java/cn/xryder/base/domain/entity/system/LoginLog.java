package cn.xryder.base.domain.entity.system;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Author: joetao
 * @Date: 2024/9/12 10:57
 */
@Entity
@Table(name = "sys_login_log")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String nickname;

    private LocalDateTime loginDate;

    private boolean success;
}
