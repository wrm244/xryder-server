package cn.xryder.base.domain.entity.system;

import cn.xryder.base.domain.entity.BaseDO;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: joetao
 * @Date: 2024/9/24 16:06
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_notification")
@Data
public class Notification extends BaseDO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String content;

    /**
     * 1: 按部门发送 2：发送所有人
     */
    private Integer type;
}
