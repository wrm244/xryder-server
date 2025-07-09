package cn.xryder.base.controller;

import cn.xryder.base.common.RsaUtil;
import cn.xryder.base.domain.R;
import cn.xryder.base.domain.ResultCode;
import cn.xryder.base.service.JwtService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author wrm244
 */
@RequestMapping("/api/v1")
@RestController
public class OpenController {
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public OpenController(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @GetMapping("/publicKey")
    public R<String> getPublicKey() {
        return R.ok(RsaUtil.getPublicKey());
    }

    @PostMapping("/refreshToken")
    public R<String> getToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        if (StringUtils.isBlank(refreshToken)) {
            return R.error(ResultCode.INVALID_REFRESH_TOKEN, "refreshToken不能为空");
        }

        if (!jwtService.isValidRefreshToken(refreshToken)) {
            return R.error(ResultCode.INVALID_REFRESH_TOKEN, "无效的refreshToken");
        }

        try {
            // 使用专门的RefreshToken用户名提取方法
            String username = jwtService.extractUsernameFromRefreshToken(refreshToken);
            if (StringUtils.isBlank(username)) {
                return R.error(ResultCode.INVALID_REFRESH_TOKEN, "无法从refreshToken中提取用户信息");
            }

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            String authorities = StringUtils.join(userDetails.getAuthorities(), ",");
            String accessToken = jwtService.generateToken(username, authorities);

            return R.ok(accessToken);
        } catch (Exception e) {
            return R.error(ResultCode.INVALID_REFRESH_TOKEN, "生成访问令牌失败: " + e.getMessage());
        }
    }
}
