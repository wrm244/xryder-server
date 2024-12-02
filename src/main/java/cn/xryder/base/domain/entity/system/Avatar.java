package cn.xryder.base.domain.entity.system;

import cn.xryder.base.domain.entity.BaseDO;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author: joetao
 * @Date: 2024/8/16 14:17
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "sys_avatar")
@Data
public class Avatar extends BaseDO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 头像文件名
     */
    private String fileName;

    /**
     * 头像base64
     */
    private String fileData;
}
