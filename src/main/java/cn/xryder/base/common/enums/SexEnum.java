package cn.xryder.base.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 性别的枚举值
 *
 * @author les
 */
@Getter
@AllArgsConstructor
public enum SexEnum {

    /**
     * 男
     */
    MALE(1),
    /**
     * 女
     */
    FEMALE(2),
    /* 未知 */
    UNKNOWN(0);

    /**
     * 性别
     */
    private final Integer sex;

}
