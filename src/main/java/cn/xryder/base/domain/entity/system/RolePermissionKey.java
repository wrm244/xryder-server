package cn.xryder.base.domain.entity.system;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: joetao
 * @Date: 2024/8/20 9:59
 */
@Embeddable
@Data
public class RolePermissionKey implements Serializable {
    private Long roleId;
    private Long permissionId;

}
