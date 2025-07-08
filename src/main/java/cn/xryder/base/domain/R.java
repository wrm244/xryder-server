package cn.xryder.base.domain;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * RESTful API 返回类型
 * 
 * @author wrm244
 * 
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class R<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    // 预定义常用的成功响应，避免重复创建
    private static final R<Void> SUCCESS_EMPTY = new R<>(ResultCode.SUCCESS, null);

    private int code;
    private String msg;
    private String requestId;
    private T data;

    // 缓存JSON字符串，避免重复序列化
    private transient String cachedJsonString;

    public R(ResultCode resultCode) {
        setResultCode(resultCode);
    }

    public R(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public R(ResultCode resultCode, T data) {
        setResultCode(resultCode);
        this.data = data;
    }

    public R(ResultCode resultCode, T data, String requestId) {
        setResultCode(resultCode);
        this.data = data;
        this.requestId = requestId;
    }

    // 优化：返回预定义的成功响应
    @SuppressWarnings("unchecked")
    public static <T> R<T> ok() {
        return (R<T>) SUCCESS_EMPTY;
    }

    public static <T> R<T> ok(T data) {
        return new R<>(ResultCode.SUCCESS, data);
    }

    public static <T> R<T> ok(T data, String requestId) {
        return new R<>(ResultCode.SUCCESS, data, requestId);
    }

    public static <T> R<T> failure(ResultCode code) {
        return new R<>(code, null);
    }

    public static <T> R<T> failure(ResultCode code, T data) {
        return new R<>(code, data);
    }

    public static <T> R<T> failure(ResultCode code, T data, String requestId) {
        return new R<>(code, data, requestId);
    }

    public static <T> R<T> failure(int code, String msg) {
        return new R<>(code, msg);
    }

    /**
     * 对象转成JSON
     */
    public String toJsonString() {
        if (cachedJsonString == null) {
            cachedJsonString = JSON.toJSONString(this);
        }
        return cachedJsonString;
    }

    public void setResultCode(ResultCode resultCode) {
        if (resultCode != null) {
            this.code = resultCode.getCode();
            this.msg = resultCode.getMsg();
            // 清除缓存
            this.cachedJsonString = null;
        }
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return ResultCode.SUCCESS.getCode() == this.code;
    }

    /**
     * 判断是否失败
     */
    public boolean isFailure() {
        return !isSuccess();
    }

    // 优化toString，使用StringBuilder
    @Override
    public String toString() {
        return new StringBuilder(128)
                .append("{\"code\":").append(code)
                .append(", \"msg\":\"").append(msg).append('\"')
                .append(", \"data\":\"").append(data).append('\"')
                .append('}')
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        R<?> r = (R<?>) o;
        return code == r.code &&
                Objects.equals(msg, r.msg) &&
                Objects.equals(requestId, r.requestId) &&
                Objects.equals(data, r.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, msg, requestId, data);
    }

    // 重写setter方法，清除缓存
    public void setCode(int code) {
        this.code = code;
        this.cachedJsonString = null;
    }

    public void setMsg(String msg) {
        this.msg = msg;
        this.cachedJsonString = null;
    }

    public void setData(T data) {
        this.data = data;
        this.cachedJsonString = null;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
        this.cachedJsonString = null;
    }
}