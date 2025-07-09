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
        log.debug("原始密码: {}", password);
        try {
            if (passwordEncrypted) {
                // 先进行URL解码，然后再进行RSA解密
                String urlDecodedPassword = java.net.URLDecoder.decode(password, "UTF-8");
                log.debug("URL解码后的密码: {}", urlDecodedPassword);
                password = RsaUtil.decryptWithPrivate(urlDecodedPassword);
                log.debug("RSA解密后的密码: {}", password);
            }
        } catch (Exception e) {
            log.error("密码解密失败！原始密码: {}", authentication.getCredentials().toString(), e);
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

    /**
     * 用于调试的方法，测试密码解密流程
     */
    public static void testPasswordDecryption(String encryptedPassword) {
        try {
            log.info("原始加密密码: {}", encryptedPassword);

            // 先进行URL解码
            String urlDecodedPassword = java.net.URLDecoder.decode(encryptedPassword, "UTF-8");
            log.info("URL解码后的密码: {}", urlDecodedPassword);

            // 再进行RSA解密
            String decryptedPassword = RsaUtil.decryptWithPrivate(urlDecodedPassword);
            log.info("RSA解密后的密码: {}", decryptedPassword);

        } catch (Exception e) {
            log.error("测试密码解密失败", e);
        }
    }
}
