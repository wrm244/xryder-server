package cn.xryder.base.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: joetao
 * @Date: 2024/9/26 15:20
 */
@Data
@Builder
public class MailVO {
    private Long id;
    private String title;
    private String content;
    private Boolean hasRead;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime sendTime;
}
