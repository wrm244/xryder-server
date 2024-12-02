package cn.xryder.base.domain.dto.system;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @Author: joetao
 * @Date: 2024/9/19 14:19
 */
@Data
public class PositionDTO {
    private Long id;
    @Size(min = 2, max = 12, message = "职位名称长度为 2-12")
    private String name;
    @Size(min = 6, max = 32, message = "描述信息长度为 6-32")
    private String description;
    private Long deptId;
    private String deptName;
}
