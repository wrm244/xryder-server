package cn.xryder.base.domain;

import cn.hutool.core.util.ObjectUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serial;
import java.util.HashMap;
import java.util.Objects;

/**
 * 统一响应数据类型
 *
 * @author wrm244
 */
@Schema(description = "统一响应结果")
public class R<T> extends HashMap<String, Object> {
    /**
     * 状态码
     */
    @Schema(description = "状态码")
    public static final String CODE_TAG = "code";
    /**
     * 返回内容
     */
    @Schema(description = "返回消息")
    public static final String MSG_TAG = "msg";
    /**
     * 数据对象
     */
    @Schema(description = "返回数据")
    public static final String DATA_TAG = "data";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * 构造器
     */
    public R() {
    }

    public R(int code, String msg) {
        super.put(CODE_TAG, code);
        super.put(MSG_TAG, msg);
    }

    public R(int code, String msg, T data) {
        super.put(CODE_TAG, code);
        super.put(MSG_TAG, msg);
        if (ObjectUtil.isNotNull(data)) {
            super.put(DATA_TAG, data);
        }
    }

    /**
     * 返回成功消息
     */
    public static <T> R<T> ok(String msg, T data) {
        return new R<>(ResultCode.SUCCESS.getCode(), msg, data);
    }

    public static <T> R<T> ok(T data) {
        return R.ok(ResultCode.SUCCESS.getMsg(), data);
    }

    public static <T> R<T> ok() {
        return new R<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMsg());
    }

    public static <T> R<T> okMsg(String msg) {
        return R.ok(msg, null);
    }

    /**
     * 返回警告信息
     */
    public static <T> R<T> warn(ResultCode code, String msg, T data) {
        return new R<>(code.getCode(), msg, data);
    }

    public static <T> R<T> warn() {
        return R.warn(ResultCode.BAD_REQUEST, ResultCode.BAD_REQUEST.getMsg());
    }

    public static <T> R<T> warn(ResultCode code, String msg) {
        return R.warn(code, msg, null);
    }

    public static <T> R<T> warn(String msg) {
        return R.warn(ResultCode.BAD_REQUEST, msg);
    }

    public static <T> R<T> warn(int code, String msg) {
        return new R<>(code, msg);
    }

    /**
     * 返回错误信息
     */
    public static <T> R<T> error(ResultCode code, String msg, T data) {
        return new R<>(code.getCode(), msg, data);
    }

    public static <T> R<T> error(ResultCode code) {
        return new R<>(code.getCode(), code.getMsg());
    }

    public static <T> R<T> error() {
        return R.error(ResultCode.SERVER_ERROR);
    }

    public static <T> R<T> error(ResultCode code, String msg) {
        return R.error(code, msg, null);
    }

    public static <T> R<T> error(String msg) {
        return R.error(ResultCode.SERVER_ERROR, msg);
    }

    public static <T> R<T> error(int code, String msg) {
        return new R<>(code, msg);
    }


    @Override
    public String toString() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            // 可以记录日志或返回默认值
            return "{}";
        }
    }

    /**
     * 检验消息类型
     */
    public boolean isSuccess() {
        return Objects.equals(ResultCode.SUCCESS.getCode(), this.get(CODE_TAG));
    }

    public boolean isWarn() {
        Object code = this.get(CODE_TAG);
        return code != null && Objects.equals(ResultCode.BAD_REQUEST.getCode(), code);
    }

    public boolean isError() {
        Object code = this.get(CODE_TAG);
        if (code == null) {
            return false;
        }
        // 判断是否为错误状态码 (4xx 和 5xx，但排除 400)
        if (code instanceof Integer codeValue) {
            return codeValue >= 401;
        }
        return false;
    }

    /**
     * 重写HashMap的put方法,方便链式编程
     */
    @Override
    public R<T> put(String key, Object value) {
        super.put(key, value);
        return this;
    }
}
