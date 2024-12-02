package cn.xryder.base.domain.entity.system;

import cn.xryder.base.domain.entity.BaseDO;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

/**
 * @Author: joetao
 * @Date: 2024/8/2 10:36
 */
@Table(name = "sys_department")
@Entity
@EqualsAndHashCode(of = "id")
@Data
@ToString(exclude = "childrenDepartments")
public class Department extends BaseDO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(length = 50, nullable = false)
    private String name;
    @Column(length = 100)
    private String description;
    @Column
    private Integer position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Department parentDepartment;

    @OneToMany(mappedBy = "parentDepartment", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private Set<Department> childrenDepartments = new HashSet<>();

}