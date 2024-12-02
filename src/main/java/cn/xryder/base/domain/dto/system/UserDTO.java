package cn.xryder.base.domain.dto.system;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * @Author: joetao
 * @Date: 2024/8/12 16:06
 */
@Data
public class UserDTO {
    @NotBlank(message = "用户账号不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9]{4,30}$", message = "用户账号由 数字、字母 组成")
    @Size(min = 4, max = 30, message = "用户账号长度为 4-30 个字符")
    private String username;
    @Size(min = 2, max = 16, message = "用户昵称长度为 2-16 个字符")
    private String nickname;
    private String remark;
    private Long deptId;
    private String email;
    private String mobile;
    private Integer sex;
    private String avatar;
    @Pattern(regexp = "^[a-zA-Z0-9_]{4,16}$", message = "用户账号由 数字、字母、下划线 组成")
    @Length(min = 4, max = 16, message = "密码长度为 4-16 位")
    private String password;
}
