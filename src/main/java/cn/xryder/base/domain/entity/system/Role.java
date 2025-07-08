package cn.xryder.base.domain.entity.system;

import cn.xryder.base.common.enums.RoleTypeEnum;
import cn.xryder.base.domain.entity.BaseDO;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: joetao
 * @Date: 2024/7/30 16:04
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_role")
@Data
public class Role extends BaseDO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 角色名称
     */
    private String name;
    /**
     * 级别， 0 - 10。数字越小，级别越高，高级别角色包含低级别角色的所有权限。
     */
    private Integer level;
    /**
     * 角色类型
     * <p>
     * 枚举 {@link RoleTypeEnum}
     */
    private Integer type;
    /**
     * 备注
     */
    private String remark;
}
