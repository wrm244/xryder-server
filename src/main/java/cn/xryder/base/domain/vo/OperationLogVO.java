package cn.xryder.base.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: joetao
 * @Date: 2024/9/13 11:23
 */
@Data
public class OperationLogVO {
    private Long id;
    private String content;
    private String methodName;
    private String requestParams;
    private String operator;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private LocalDateTime operationTime;
    private long timeTaken;
}
