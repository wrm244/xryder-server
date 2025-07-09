package cn.xryder.base.domain;

import lombok.Getter;

/**
 * @author Joetao
 * 状态码
 * Created by jt on 2018/3/8.
 */

@Getter
public enum ResultCode {
    /*
    请求返回状态码和说明信息
     */
    SUCCESS(200, "成功"),
    BAD_REQUEST(400, "参数或者语法不对"),
    UNAUTHORIZED(401, "认证失败"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "请求的资源不存在"),
    INVALID_REFRESH_TOKEN(406, "无效的refreshToken"),
    TOKEN_EXPIRED(405, "token过期"),
    CONFLICT(409, "资源已存在"),
    SQL_ERROR(410, "sql执行错误"),
    SERVER_ERROR(500, "服务器内部错误");
    private final int code;
    private final String msg;

    ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

}
