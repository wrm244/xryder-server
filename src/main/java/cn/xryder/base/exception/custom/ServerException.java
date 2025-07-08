package cn.xryder.base.exception.custom;

/**
 * @author wrm244
 */
public class ServerException extends BaseException {
    public ServerException(String message) {
        super(message);
    }

    public ServerException(String message, Throwable e) {
        super(message, e);
    }
}
