package cn.xryder.base.domain.entity.system;

import cn.xryder.base.domain.entity.BaseDO;
import jakarta.persistence.*;
import lombok.*;

/**
 * @Author: joetao
 * @Date: 2024/9/19 14:22
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_position")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Position extends BaseDO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private Long deptId;
    private String deptName;
}
