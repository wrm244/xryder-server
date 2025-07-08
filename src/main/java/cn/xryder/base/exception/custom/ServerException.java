package cn.xryder.base.exception.custom;

/**
 * @author Joetao
 * @date 2023/11/29
 */
public class ServerException extends BaseException {
    public ServerException(String message) {
        super(message);
    }

    public ServerException(String message, Throwable e) {
        super(message, e);
    }
}
