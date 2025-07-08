package cn.xryder.base.domain.vo;

/**
 * @Author: joetao
 * @Date: 2024/8/2 15:17
 */

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentVO {

    private Long id;
    private String name;
    private String description;
    private Integer position;
    private Set<DepartmentVO> children;

}

