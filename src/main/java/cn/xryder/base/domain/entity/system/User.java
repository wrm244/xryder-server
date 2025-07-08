package cn.xryder.base.domain.entity.system;

import cn.xryder.base.common.CommonStatusEnum;
import cn.xryder.base.common.SexEnum;
import cn.xryder.base.domain.entity.BaseDO;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;

/**
 * @Author: joetao
 * @Date: 2024/7/30 16:01
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_user")
@Data
public class User extends BaseDO {
    /**
     * 用户账号
     */
    @Id
    private String username;
    /**
     * 用户昵称
     */
    private String nickname;
    /**
     * 加密后的密码
     * <p>
     * 因为目前使用 {@link BCryptPasswordEncoder} 加密器，所以无需自己处理 salt 盐
     */
    private String password;
    /**
     * 帐号状态
     * <p>
     * 枚举 {@link CommonStatusEnum}
     */
    private boolean enabled;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 手机号码
     */
    private String mobile;
    /**
     * 备注
     */
    private String remark;

    /**
     * 用户性别
     * <p>
     * 枚举类 {@link SexEnum}
     */
    private Integer sex;
    /**
     * 用户头像base64
     */
    private String avatar;

    /**
     * 部门 ID
     */
    private Long departmentId;
    /**
     * 职位 ID
     */
    private Long positionId;
    /**
     * 最后登录IP
     */
    private String loginIp;
    /**
     * 最后登录时间
     */
    private LocalDateTime loginDate;

    // 切换用户启用/禁用状态
    public void toggleEnabled() {
        this.enabled = !this.enabled;
    }
}
