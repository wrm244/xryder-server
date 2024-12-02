package cn.xryder.base.domain.dto.system;

import lombok.Data;

/**
 * @Author: joetao
 * @Date: 2024/9/23 14:22
 */
@Data
public class UserSettingDTO {
    private String username;

    private Long deptId;

    private Long positionId;
}
