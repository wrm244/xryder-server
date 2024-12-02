package cn.xryder.base.domain.vo;

import lombok.Data;

/**
 * @Author: joetao
 * @Date: 2024/9/19 14:19
 */
@Data
public class PositionVO {
    private Long id;
    private String name;
    private String description;
    private Long deptId;
    private String deptName;
}
