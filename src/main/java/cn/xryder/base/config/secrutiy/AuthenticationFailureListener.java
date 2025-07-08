package cn.xryder.base.config.secrutiy;

import cn.xryder.base.domain.entity.system.LoginLog;
import cn.xryder.base.repo.system.LoginLogRepo;
import cn.xryder.base.service.LoginAttemptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 登录失败监听，防暴力破解
 *
 * @Author: joetao
 * @Date: 2024/8/1 15:09
 */
@Component
@Slf4j
public class AuthenticationFailureListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {
    private final LoginAttemptService loginAttemptService;
    private final LoginLogRepo loginLogRepo;

    public AuthenticationFailureListener(LoginAttemptService loginAttemptService, LoginLogRepo loginLogRepo) {
        this.loginAttemptService = loginAttemptService;
        this.loginLogRepo = loginLogRepo;
    }

    @Override
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent event) {
        String username = event.getAuthentication().getName();
        log.warn("登录失败，用户名：{}", username);
        LoginLog loginLog = LoginLog.builder()
                .username(username)
                .loginDate(LocalDateTime.now())
                .success(false)
                .build();
        loginLogRepo.save(loginLog);
        loginAttemptService.loginFailed(username);
    }

    @Override
    public boolean supportsAsyncExecution() {
        return ApplicationListener.super.supportsAsyncExecution();
    }
}
