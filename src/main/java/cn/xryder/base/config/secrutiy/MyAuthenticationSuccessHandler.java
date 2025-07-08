package cn.xryder.base.config.secrutiy;

import cn.xryder.base.domain.LoginUser;
import cn.xryder.base.domain.R;
import cn.xryder.base.domain.entity.system.LoginLog;
import cn.xryder.base.domain.entity.system.User;
import cn.xryder.base.domain.vo.AccessTokenVO;
import cn.xryder.base.repo.system.LoginLogRepo;
import cn.xryder.base.repo.system.UserRepo;
import cn.xryder.base.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @Author: joetao
 * @Date: 2024/4/18 15:18
 */
@Component
@Slf4j
public class MyAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtService jwtService;
    private final UserRepo userRepo;
    private final LoginLogRepo loginLogRepo;

    public MyAuthenticationSuccessHandler(JwtService jwtService, UserRepo userRepo, LoginLogRepo loginLogRepo) {
        this.jwtService = jwtService;
        this.userRepo = userRepo;
        this.loginLogRepo = loginLogRepo;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        log.info("登录成功, 用户: {}", authentication.getPrincipal());
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Optional<User> userOptional = userRepo.findById(loginUser.getUsername());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setLoginDate(LocalDateTime.now());
            user.setLoginIp(getClientIp(request));
            userRepo.save(user);
        }
        String token = jwtService.GenerateToken(loginUser.getUsername(),
                StringUtils.join(loginUser.getAuthorities(), ","));
        String refreshToken = jwtService.GenerateRefreshToken(loginUser.getUsername());
        List<String> permissions = loginUser.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        AccessTokenVO accessTokenVO =
                new AccessTokenVO(loginUser.getUsername(), loginUser.getName(), permissions, token, refreshToken);
        LoginLog loginLog = LoginLog.builder()
                .nickname(loginUser.getName())
                .username(loginUser.getUsername())
                .loginDate(LocalDateTime.now())
                .success(true)
                .build();
        loginLogRepo.save(loginLog);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        PrintWriter printWriter = response.getWriter();
        String body = R.ok(accessTokenVO).toJsonString();
        printWriter.write(body);
        printWriter.flush();
    }

    public String getClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }
}
