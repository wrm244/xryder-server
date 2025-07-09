package cn.xryder.base.controller;

import cn.xryder.base.common.RsaUtil;
import cn.xryder.base.domain.R;
import cn.xryder.base.domain.ResultCode;
import cn.xryder.base.service.JwtService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/token")
    public R<String> getToken(@RequestParam("refreshToken") String refreshToken) {
        boolean isRefreshToken = jwtService.isValidRefreshToken(refreshToken);
        if (!isRefreshToken) {
            return R.error(ResultCode.INVALID_REFRESH_TOKEN, "无效的refreshToken");
        }
        String username = jwtService.extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return R.ok(jwtService.GenerateToken(username,
                StringUtils.join(userDetails.getAuthorities(), ",")));
    }
}
