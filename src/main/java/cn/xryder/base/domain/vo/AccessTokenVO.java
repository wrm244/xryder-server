package cn.xryder.base.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * @Author: joetao
 * @Date: 2024/8/15 16:46
 */
@Data
@AllArgsConstructor
public class AccessTokenVO {
    private String username;
    private String nickname;
    private List<String> permissions;
    private String token;
    private String refreshToken;
}
