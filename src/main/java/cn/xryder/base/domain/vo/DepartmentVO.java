package cn.xryder.base.domain.vo;


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

