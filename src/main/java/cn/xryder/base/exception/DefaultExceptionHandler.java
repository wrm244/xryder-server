package cn.xryder.base.exception;

import cn.xryder.base.domain.R;
import cn.xryder.base.domain.ResultCode;
import cn.xryder.base.exception.custom.*;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 异常处理类
 *
 * @author wrm244
 */
@RestControllerAdvice
@Slf4j
public class DefaultExceptionHandler {
    @ExceptionHandler({BadRequestException.class})
    public R<?> handleBadRequestException(BadRequestException e) {
        return R.failure(ResultCode.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler({NotAllowedException.class})
    public R<?> handleNotAllowedException(NotAllowedException e) {
        return R.failure(ResultCode.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler({ResourceNotFoundException.class})
    public R<?> handleResourceNotFoundException(ResourceNotFoundException e) {
        return R.failure(ResultCode.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler({ResourceConflictException.class})
    public R<?> handleResourceConflictException(ResourceConflictException e) {
        return R.failure(ResultCode.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public R<?> handleExpiredJwtException(ExpiredJwtException e) {
        return R.failure(ResultCode.TOKEN_EXPIRED, e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public R<?> handleAccessDeniedException(AccessDeniedException e) {
        return R.failure(ResultCode.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler({ServerException.class})
    public R<?> handleServerException(Exception e) {
        log.error(e.getMessage());
        return R.failure(ResultCode.SERVER_ERROR, e.getMessage());
    }

}
