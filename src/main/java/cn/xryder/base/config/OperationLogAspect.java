package cn.xryder.base.config;

import cn.xryder.base.service.system.LogService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 拦截标注了 @OperationLog 注解的方法，记录操作日志
 *
 * @author wrm244
 */
@Aspect
@Component
@Slf4j
public class OperationLogAspect {
    private final LogService logService;
    private final TaskExecutor logTaskExecutor;

    public OperationLogAspect(LogService logService, @Qualifier("logTaskExecutor") TaskExecutor logTaskExecutor) {
        this.logService = logService;
        this.logTaskExecutor = logTaskExecutor;
    }

    @Pointcut("@annotation(cn.xryder.base.config.OperationLog)")
    public void logPointCut() {
    }

    @Around("logPointCut() && @annotation(operationLog)")
    public Object logAround(ProceedingJoinPoint joinPoint, OperationLog operationLog) throws Throwable {
        // 获取操作描述信息
        String operationDescription = operationLog.value();
        // 获取方法的参数
        Object[] args = joinPoint.getArgs();
        String argsString = Arrays.toString(args);
        // 获取操作者用户名（使用 Spring Security）
        String username = getCurrentUsername();
        // 获取请求的方法信息
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getName();
        String fullMethodName = className + "." + methodName;

        // 异步记录操作开始日志
        logTaskExecutor.execute(() -> log.info("操作开始: {} [用户: {}, 方法: {}]", operationDescription, username, fullMethodName));

        Object result;
        long startTime = System.currentTimeMillis();
        try {
            // 执行目标方法
            result = joinPoint.proceed();
        } catch (Exception e) {
            // 异步记录失败日志
            long timeTaken = System.currentTimeMillis() - startTime;
            logTaskExecutor.execute(() -> {
                log.error("操作失败: {} [用户: {}, 方法: {}, 耗时: {} ms]",
                        operationDescription, username, fullMethodName, timeTaken, e);
                logService.saveOperationLog(operationDescription, fullMethodName, argsString, username, -1);
            });
            throw e;
        }

        long timeTaken = System.currentTimeMillis() - startTime;
        // 异步记录操作完成日志和保存到数据库
        logTaskExecutor.execute(() -> {
            log.info("操作完成: {} [用户: {}, 方法: {}, 耗时: {} ms]",
                    operationDescription, username, fullMethodName, timeTaken);
            logService.saveOperationLog(operationDescription, fullMethodName, argsString, username, timeTaken);
        });

        return result;
    }

    /**
     * 获取当前用户名
     */
    private String getCurrentUsername() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof UserDetails userDetails) {
                return userDetails.getUsername();
            } else {
                return principal.toString();
            }
        } catch (Exception e) {
            log.warn("获取当前用户信息失败", e);
            return "unknown";
        }
    }
}
