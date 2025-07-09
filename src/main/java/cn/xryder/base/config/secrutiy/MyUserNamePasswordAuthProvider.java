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
 * 自定义用户名密码认证
 * 
 * @author wrm244
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
        log.debug("开始认证用户: {}", username);

        // 使用 char[] 来存储明文密码，减少内存暴露时间
        char[] plainPassword = null;
        try {
            if (passwordEncrypted) {
                // 先进行URL解码，然后再进行RSA解密
                String urlDecodedPassword = java.net.URLDecoder.decode(password, "UTF-8");
                log.debug("密码解密处理中...");
                String decryptedPassword = RsaUtil.decryptWithPrivate(urlDecodedPassword);
                plainPassword = decryptedPassword.toCharArray();

                // 立即清除解密后的字符串
                decryptedPassword = null;
                password = new String(plainPassword);
            } else {
                plainPassword = password.toCharArray();
            }
        } catch (Exception e) {
            log.error("密码解密失败！用户: {}", username);
            // 清理可能的敏感数据
            if (plainPassword != null) {
                java.util.Arrays.fill(plainPassword, '\0');
            }
            throw new BadCredentialsException("账号或密码错误！");
        }

        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            boolean matches = passwordEncoder.matches(password, userDetails.getPassword());

            if (matches) {
                log.debug("用户 {} 认证成功", username);
                return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            } else {
                log.debug("用户 {} 密码不匹配", username);
                throw new BadCredentialsException("账号或密码错误！");
            }
        } finally {
            // 确保清理敏感数据
            if (plainPassword != null) {
                java.util.Arrays.fill(plainPassword, '\0');
            }
            // 清除密码变量
            password = null;
        }
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
