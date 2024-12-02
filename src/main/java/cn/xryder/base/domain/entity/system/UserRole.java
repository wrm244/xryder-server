package cn.xryder.base.domain.entity.system;

import cn.xryder.base.domain.entity.BaseDO;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: joetao
 * @Date: 2024/8/12 15:46
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_user_role")
@Data
public class UserRole extends BaseDO {

    /**
     * 自增主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 用户 ID
     */
    private String username;
    /**
     * 角色 ID
     */
    private Long roleId;

}
