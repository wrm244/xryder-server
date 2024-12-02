package cn.xryder.base.exception;

import cn.xryder.base.domain.ResultCode;
import cn.xryder.base.domain.ResultJson;
import cn.xryder.base.exception.custom.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 异常处理类
 *
 * @author jt
 */
@RestControllerAdvice
@Slf4j
public class DefaultExceptionHandler {
    @ExceptionHandler({BadRequestException.class})
    public ResultJson<?> handleBadRequestException(BadRequestException e) {
        return ResultJson.failure(ResultCode.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler({NotAllowedException.class})
    public ResultJson<?> handleBadRequestException(NotAllowedException e) {
        return ResultJson.failure(ResultCode.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler({ResourceNotFoundException.class})
    public ResultJson<?> handleResourceNotFoundException(ResourceNotFoundException e) {
        return ResultJson.failure(ResultCode.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler({ResourceConflictException.class})
    public ResultJson<?> handleResourceConflictException(ResourceConflictException e) {
        return ResultJson.failure(ResultCode.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResultJson<?> handleAccessDeniedException(AccessDeniedException e) {
        return ResultJson.failure(ResultCode.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler({ServerException.class})
    public ResultJson<?> handleServerException(Exception e) {
        log.error(e.getMessage());
        return ResultJson.failure(ResultCode.SERVER_ERROR, e.getMessage());
    }

}
