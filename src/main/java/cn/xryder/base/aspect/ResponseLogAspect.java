package cn.xryder.base.aspect;

import cn.xryder.base.domain.R;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 响应日志切面
 * 拦截所有控制器返回的R对象，异步记录请求和响应的日志
 *
 * @author wrm244
 */
@Aspect
@Component
@Slf4j
public class ResponseLogAspect {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .disable(SerializationFeature.INDENT_OUTPUT);

    private static final String DIVIDER = "------------------------------------------------------------";
    private static final String REQUEST_PREFIX = ">>>>> 请求";
    private static final String RESPONSE_PREFIX = "<<<<< 响应";

    // 用于跟踪请求计数
    private static final AtomicLong REQUEST_COUNTER = new AtomicLong(0);

    // 定义日期格式化器为静态常量，避免重复创建
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 存储请求信息的线程安全Map，用于后续响应时获取请求信息
    private static final ConcurrentHashMap<Long, RequestInfo> REQUEST_INFO_MAP = new ConcurrentHashMap<>();
    // 后台任务
    private final TaskExecutor taskExecutor;

    public ResponseLogAspect(@Qualifier("logTaskExecutor") TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    /**
     * 定义切点，拦截所有返回R类型的控制器方法
     */
    @Pointcut("execution(cn.xryder.base.domain.R *..controller..*.*(..))")
    public void responsePointcut() {
    }

    /**
     * 请求前记录日志
     */
    @Before("responsePointcut()")
    public void logBefore(JoinPoint joinPoint) {
        // 先检查日志级别，避免不必要的处理
        if (!log.isInfoEnabled()) {
            return;
        }

        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                Long requestId = REQUEST_COUNTER.incrementAndGet();

                // 存储请求ID供后续使用
                request.setAttribute("requestId", requestId);

                // 创建请求信息对象
                RequestInfo requestInfo = new RequestInfo();
                requestInfo.setRequestId(requestId);
                requestInfo.setIpAddress(getClientIp(request));
                requestInfo.setRequestTime(getCurrentTime());
                requestInfo.setStartTimeMillis(System.currentTimeMillis());
                requestInfo.setMethod(request.getMethod());
                requestInfo.setUri(request.getRequestURI());
                requestInfo.setParams(safelyProcessRequestParams(joinPoint.getArgs()));

                // 获取方法调用的源信息
                String classMethod = extractClassAndMethod(joinPoint);
                requestInfo.setClassMethod(classMethod);

                // 存储请求信息
                REQUEST_INFO_MAP.put(requestId, requestInfo);

                // 异步执行日志记录
                final ServletRequestAttributes finalAttributes = attributes;
                taskExecutor.execute(() -> {
                    // 复制请求上下文
                    RequestContextHolder.setRequestAttributes(finalAttributes, true);
                    try {
                        // 使用StringBuilder构建日志
                        String sb = '\n' + DIVIDER + '\n' +
                                REQUEST_PREFIX + " [" + requestId + "]\n" +
                                "| IP地址: " + requestInfo.getIpAddress() + '\n' +
                                "| 时间: " + requestInfo.getRequestTime() + '\n' +
                                "| 请求方式: " + requestInfo.getMethod() + '\n' +
                                "| 请求路径: " + requestInfo.getUri() + '\n' +
                                "| 调用方法: " + requestInfo.getClassMethod() + '\n' +
                                "| 请求参数: " + requestInfo.getParams() + '\n' +
                                DIVIDER;
                        log.info(sb);
                    } finally {
                        RequestContextHolder.resetRequestAttributes();
                    }
                });
            }
        } catch (Exception e) {
            log.error("记录请求日志失败", e);
        }
    }

    /**
     * 方法返回后记录日志
     */
    @AfterReturning(pointcut = "responsePointcut()", returning = "result")
    public void logAfterReturning(Object result) {
        if (!(result instanceof R<?> r)) {
            return;
        }

        try {
            // 获取请求ID
            Long requestId = null;
            RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
            if (attributes instanceof ServletRequestAttributes) {
                HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();
                requestId = (Long) request.getAttribute("requestId");
            }

            final Long finalRequestId = requestId;
            final RequestAttributes finalAttributes = attributes;

            // 异步执行日志记录
            taskExecutor.execute(() -> {
                try {
                    // 复制请求上下文
                    RequestContextHolder.setRequestAttributes(finalAttributes, true);

                    // 根据响应状态预先确定日志级别
                    boolean isErrorLog = r.isFailure() && log.isErrorEnabled();
                    boolean isWarnLog = r.isWarn() && log.isWarnEnabled();
                    boolean isInfoLog = !r.isFailure() && !r.isWarn() && log.isInfoEnabled();

                    if (!isErrorLog && !isWarnLog && !isInfoLog) {
                        return;
                    }

                    String responseLog = buildResponseLogMessage(r, finalRequestId);

                    // 根据不同日志级别记录日志
                    if (isErrorLog) {
                        log.error(responseLog);
                    } else if (isWarnLog) {
                        log.warn(responseLog);
                    } else {
                        log.info(responseLog);
                    }

                    // 记录完成后清理请求信息
                    if (finalRequestId != null) {
                        REQUEST_INFO_MAP.remove(finalRequestId);
                    }
                } finally {
                    RequestContextHolder.resetRequestAttributes();
                }
            });
        } catch (Exception e) {
            log.error("记录响应日志失败", e);
        }
    }

    /**
     * 构建响应日志消息
     */
    private String buildResponseLogMessage(R<?> r, Long requestId) {
        String status;
        if (r.isFailure()) {
            status = "错误";
        } else if (r.isWarn()) {
            status = "警告";
        } else if (r.isSuccess()) {
            status = "成功";
        } else {
            status = "未知";
        }
        long duration = 0;
        StringBuilder logBuilder = new StringBuilder(512)
                .append('\n').append(DIVIDER).append('\n')
                .append(RESPONSE_PREFIX);

        if (requestId != null) {
            logBuilder.append(" [").append(requestId).append("]");

            // 从缓存中获取请求信息
            RequestInfo requestInfo = REQUEST_INFO_MAP.get(requestId);

            if (requestInfo != null) {
                // 计算耗时（毫秒）
                duration = System.currentTimeMillis() - requestInfo.getStartTimeMillis();

                logBuilder.append('\n')
                        .append("| IP地址: ").append(requestInfo.getIpAddress()).append('\n')
                        .append("| 请求时间: ").append(requestInfo.getRequestTime()).append('\n');

            }
        }

        logBuilder.append('\n')
                .append("| 响应时间: ").append(getCurrentTime()).append('\n')
                .append("| 响应耗时: ").append(duration).append("ms").append('\n')
                .append("| 响应状态: ").append(status).append('\n')
                .append("| 响应内容: ").append(safelyFormatResponse(r)).append('\n')
                .append(DIVIDER);

        return logBuilder.toString();
    }

    /**
     * 提取方法调用的类名和方法名
     */
    private String extractClassAndMethod(JoinPoint joinPoint) {
        try {
            Signature signature = joinPoint.getSignature();
            if (signature instanceof MethodSignature methodSignature) {
                Method method = methodSignature.getMethod();
                Class<?> declaringClass = method.getDeclaringClass();
                return declaringClass.getName() + "." + method.getName() + "()";
            }
            return joinPoint.getSignature().toShortString();
        } catch (Exception e) {
            log.error("提取方法信息失败", e);
            return "未知方法";
        }
    }

    /**
     * 安全格式化响应对象，限制内容大小
     */
    private String safelyFormatResponse(R<?> r) {
        try {
            String json = OBJECT_MAPPER.writeValueAsString(r);
            // 限制响应内容长度，避免过长导致问题
            return json.length() > 1000 ? json.substring(0, 997) + "..." : json;
        } catch (Exception e) {
            return "无法序列化响应内容，已正常响应";
        }
    }

    /**
     * 安全处理请求参数，避免处理大量数据
     */
    private String safelyProcessRequestParams(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }

        try {
            return Arrays.stream(args)
                    .map(arg -> {
                        if (arg == null) {
                            return "null";
                        } else if (arg instanceof MultipartFile file) {
                            return "File[name=" + file.getOriginalFilename() +
                                    ", size=" + file.getSize() + " bytes, type=" + file.getContentType() + "]";
                        } else {
                            try {
                                String json = OBJECT_MAPPER.writeValueAsString(arg);
                                // 限制参数内容长度
                                return json.length() > 500 ? json.substring(0, 497) + "..." : json;
                            } catch (Exception e) {
                                return "无法序列化参数: " + arg.getClass().getSimpleName();
                            }
                        }
                    })
                    .collect(Collectors.joining(", "));
        } catch (Exception e) {
            return "处理参数时出错: " + e.getMessage();
        }
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        try {
            // 优化IP获取逻辑，减少重复判断
            String[] headers = {
                    "X-Forwarded-For",
                    "Proxy-Client-IP",
                    "WL-Proxy-Client-IP",
                    "HTTP_CLIENT_IP",
                    "HTTP_X_FORWARDED_FOR"
            };

            String ip = null;
            for (String header : headers) {
                ip = request.getHeader(header);
                if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                    break;
                }
            }

            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }

            // 处理IPv6的本地环回地址
            if ("0:0:0:0:0:0:0:1".equals(ip)) {
                ip = "127.0.0.1";
            }

            return ip;
        } catch (Exception e) {
            log.error("获取客户端IP地址失败", e);
        }
        return "unknown";
    }

    /**
     * 获取当前时间字符串
     */
    private String getCurrentTime() {
        return LocalDateTime.now().format(DATE_FORMATTER);
    }

    /**
     * 请求信息存储类
     */
    @Setter
    @Getter
    private static class RequestInfo {
        private Long requestId;
        private String ipAddress;
        private String requestTime;
        private String method;
        private String uri;
        private String params;
        private String classMethod;
        private long startTimeMillis;
    }
}