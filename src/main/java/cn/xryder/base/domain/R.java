package cn.xryder.base.domain;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;

/**
 * @author Joetao
 * RESTful API 返回类型
 * Created at 2018/3/8.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class R<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 783015033603078674L;
    private int code;
    private String msg;
    private String requestId;
    private T data;

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

    public static R<Object> ok() {
        return ok(new HashMap<>(1));
    }

    public static <T> R<T> ok(T data) {
        return new R<>(ResultCode.SUCCESS, data);
    }

    public static <T> R<T> ok(T data, String requestId) {
        return new R<>(ResultCode.SUCCESS, data, requestId);
    }

    public static <T> R<T> failure(ResultCode code) {
        return failure(code, null);
    }

    public static <T> R<T> failure(ResultCode code, T o) {
        return new R<>(code, o);
    }

    public static <T> R<T> failure(ResultCode code, T o, String requestId) {
        return new R<>(code, o, requestId);
    }

    public static <T> R<T> failure(int code, String msg) {
        return new R<>(code, msg);
    }

    //对象转成JSON
    public String toJsonString() {
        return JSON.toJSONString(this);
    }

    public void setResultCode(ResultCode resultCode) {
        this.code = resultCode.getCode();
        this.msg = resultCode.getMsg();
    }

    @Override
    public String toString() {
        return "{" +
                "\"code\":" + code +
                ", \"msg\":\"" + msg + '\"' +
                ", \"data\":\"" + data + '\"' +
                '}';
    }
}

