package cn.xryder.base.domain.entity.system;

import cn.xryder.base.domain.entity.BaseDO;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: joetao
 * @Date: 2024/8/12 15:58
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_role_permission")
@Data
public class RolePermission extends BaseDO {
    @EmbeddedId
    private RolePermissionKey id;
}
