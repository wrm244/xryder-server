package cn.xryder.base.domain.entity;

import jakarta.persistence.MappedSuperclass;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/*
 * @author: wrm244
 */
@Data
@MappedSuperclass
public class BaseDO implements Serializable {

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 最后更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建者，username
     */
    private String creator;
}
