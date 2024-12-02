package cn.xryder.base.domain.dto.system;

import lombok.Data;

/**
 * @Author: joetao
 * @Date: 2024/8/23 13:47
 */
@Data
public class UserRoleDTO {
    private String username;
    private Long[] roles;
}
