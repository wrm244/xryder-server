package cn.xryder.base.domain.dto.system;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @Author: joetao
 * @Date: 2024/8/29 10:00
 */
@Data
public class DeptDTO {
    @Size(min = 2, max = 20, message = "部门名称2-20个字符")
    private String name;
    private Integer position;
}
