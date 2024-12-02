package cn.xryder.base.domain.entity.system;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Author: joetao
 * @Date: 2024/9/13 10:06
 */
@Entity
@Table(name = "sys_operation_log")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String content;
    private String methodName;
    private String requestParams;
    private String operator;
    private LocalDateTime operationTime;
    private long timeTaken;
}

