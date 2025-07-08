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
public class ResultJson<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 783015033603078674L;
    private int code;
    private String msg;
    private String requestId;
    private T data;

    public ResultJson(ResultCode resultCode) {
        setResultCode(resultCode);
    }

    public ResultJson(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ResultJson(ResultCode resultCode, T data) {
        setResultCode(resultCode);
        this.data = data;
    }

    public ResultJson(ResultCode resultCode, T data, String requestId) {
        setResultCode(resultCode);
        this.data = data;
        this.requestId = requestId;
    }

    public static ResultJson<Object> ok() {
        return ok(new HashMap<>(1));
    }

    public static <T> ResultJson<T> ok(T data) {
        return new ResultJson<>(ResultCode.SUCCESS, data);
    }

    public static <T> ResultJson<T> ok(T data, String requestId) {
        return new ResultJson<>(ResultCode.SUCCESS, data, requestId);
    }

    public static <T> ResultJson<T> failure(ResultCode code) {
        return failure(code, null);
    }

    public static <T> ResultJson<T> failure(ResultCode code, T o) {
        return new ResultJson<>(code, o);
    }

    public static <T> ResultJson<T> failure(ResultCode code, T o, String requestId) {
        return new ResultJson<>(code, o, requestId);
    }

    public static <T> ResultJson<T> failure(int code, String msg) {
        return new ResultJson<>(code, msg);
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

