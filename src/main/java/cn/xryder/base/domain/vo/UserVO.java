package cn.xryder.base.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * @Author: joetao
 * @Date: 2024/7/31 16:05
 */
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class UserVO {
    private String username;
    private String nickname;
    private Long departmentId;
    private String department;
    private String position;
    private String email;
    private String mobile;
    private String avatar;
    private Long notificationCount;
    private boolean enabled;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private LocalDateTime loginDate;
    private String loginIp;
    private Set<RoleVO> roles;
    private List<String> permissions;
}
