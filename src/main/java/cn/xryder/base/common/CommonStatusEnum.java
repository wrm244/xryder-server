package cn.xryder.base.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 性别的枚举值
 *
 * @author les
 */
@Getter
@AllArgsConstructor
public enum CommonStatusEnum {
    /**
     * 成功
     */
    SUCCESS(1),
    /**
     * 失败
     */
    FAIL(0),
    /**
     * 开启
     */
    ENABLE(1),
    /**
     * 关闭
     */
    DISABLE(0);

    private final Integer status;

}
