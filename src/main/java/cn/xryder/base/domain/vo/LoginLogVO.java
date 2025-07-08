package cn.xryder.base.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: joetao
 * @Date: 2024/9/12 14:58
 */
@Data
public class LoginLogVO {
    private Long id;

    private String username;

    private String nickname;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime loginDate;

    private boolean success;
}
