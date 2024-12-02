package cn.xryder.base.config.secrutiy;

import cn.xryder.base.common.RsaUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @Author: joetao
 * @Date: 2024/7/31 14:29
 */
@Slf4j
public class MyUserNamePasswordAuthProvider implements AuthenticationProvider {
    private final UserDetailsService userDetailsService;
    private PasswordEncoder passwordEncoder;
    private boolean passwordEncrypted = false;
    public MyUserNamePasswordAuthProvider(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        try {
            if (passwordEncrypted) {
                password = RsaUtil.decryptWithPrivate(password);
            }
        } catch (Exception e) {
            log.error("密码解密失败！");
            throw new BadCredentialsException("账号或密码错误！");
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        boolean matches = passwordEncoder.matches(password, userDetails.getPassword());
        if (matches) {
            return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        }
        throw new BadCredentialsException("账号或密码错误！");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public void setPasswordEncrypted(boolean passwordEncrypted) {
        this.passwordEncrypted = passwordEncrypted;
    }
}
