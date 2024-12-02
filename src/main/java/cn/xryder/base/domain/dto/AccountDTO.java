package cn.xryder.base.domain.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @Author: joetao
 * @Date: 2024/8/29 16:47
 */
@Data
public class AccountDTO {
    @Size(min = 2, max = 16, message = "用户昵称长度为 2-16 个字符")
    private String nickname;
    private String remark;
    private String email;
    private String mobile;
}
