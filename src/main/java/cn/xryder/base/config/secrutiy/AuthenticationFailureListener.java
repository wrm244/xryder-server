package cn.xryder.base.config.secrutiy;

import cn.xryder.base.domain.entity.system.LoginLog;
import cn.xryder.base.repo.system.LoginLogRepo;
import cn.xryder.base.service.LoginAttemptService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 登录失败监听，防暴力破解
 *
 * @author wrm244
 */
@Component
@Slf4j
@AllArgsConstructor
public class AuthenticationFailureListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {
    private final LoginAttemptService loginAttemptService;
    private final LoginLogRepo loginLogRepo;

    @Override
    public void onApplicationEvent(@NonNull AuthenticationFailureBadCredentialsEvent event) {
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
