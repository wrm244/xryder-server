package cn.xryder.base.domain.dto.system;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @Author: joetao
 * @Date: 2024/8/20 16:06
 */
@Data
public class RoleDTO {
    private Long id;
    @Size(min = 2, max = 12, message = "角色名称长度为 2-12 个字符")
    private String name;
    private Integer level;
    private String remark;
    private Long[] permissions;
}
