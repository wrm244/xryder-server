package cn.xryder.base.domain.dto.system;

import lombok.Data;

/**
 * @Author: joetao
 * @Date: 2024/9/2 9:43
 */
@Data
public class PasswordChangeDTO {
    private String oldPassword;
    private String newPassword;
}
