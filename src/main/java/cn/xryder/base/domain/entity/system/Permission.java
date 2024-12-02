package cn.xryder.base.domain.entity.system;

import cn.xryder.base.domain.entity.BaseDO;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: joetao
 * @Date: 2024/8/12 15:56
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Table(name = "sys_permission")
public class Permission extends BaseDO {
    @Id
    private Long id;

    /**
     * 权限名称
     */
    private String name;
    /**
     * 权限标识
     *
     * 一般格式为：${系统}:${模块}:${操作}
     * 例如说：system:admin:add，即 system 服务的添加管理员。
     *
     * 当我们把该 MenuDO 赋予给角色后，意味着该角色有该资源：
     * - 对于后端，配合 @PreAuthorize 注解，配置 API 接口需要该权限，从而对 API 接口进行权限控制。
     * - 对于前端，配合前端标签，配置按钮是否展示，避免用户没有该权限时，结果可以看到该操作。
     */
    private String scope;
}
