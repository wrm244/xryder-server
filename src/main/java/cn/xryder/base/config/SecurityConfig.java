package cn.xryder.base.config;

import cn.xryder.base.config.secrutiy.*;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.ExceptionHandlingConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置
 *
 * @author wrm244
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@AllArgsConstructor
public class SecurityConfig {
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;
    private final MyAuthenticationSuccessHandler myAuthenticationSuccessHandler;
    private final MyAuthenticationFailureHandler myAuthenticationFailureHandler;
    private final UserDetailsService userDetailsService;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager)
            throws Exception {
        http.authorizeHttpRequests((
                AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authorize) -> authorize
                        .requestMatchers("/error", "/api/login", "/api/v1/publicKey", "/api/v1/refreshToken",
                                "/api/v1/ai/stream")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .csrf(AbstractHttpConfigurer::disable);
        http.formLogin(AbstractHttpConfigurer::disable);
        http.sessionManagement((SessionManagementConfigurer<HttpSecurity> session) -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.exceptionHandling((ExceptionHandlingConfigurer<HttpSecurity> exceptions) -> exceptions
                .authenticationEntryPoint(customAuthenticationEntryPoint));

        // 添加JWT认证过滤器
        http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);

        // 添加自定义的JSON登录过滤器
        http.addFilterBefore(getCustomUsernamePasswordAuthFilter(authenticationManager),
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    JsonUsernamePasswordAuthenticationFilter getCustomUsernamePasswordAuthFilter(
            AuthenticationManager authenticationManager) {
        JsonUsernamePasswordAuthenticationFilter customUsernamePasswordAuthFilter = new JsonUsernamePasswordAuthenticationFilter();
        customUsernamePasswordAuthFilter.setAuthenticationSuccessHandler(myAuthenticationSuccessHandler);
        customUsernamePasswordAuthFilter.setAuthenticationFailureHandler(myAuthenticationFailureHandler);
        customUsernamePasswordAuthFilter.setAuthenticationManager(authenticationManager);
        return customUsernamePasswordAuthFilter;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationProvider authenticationProvider() {
        MyUserNamePasswordAuthProvider myAuthenticationProvider = new MyUserNamePasswordAuthProvider(
                userDetailsService);
        myAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        // 密码是否加解密
        myAuthenticationProvider.setPasswordEncrypted(true);
        return myAuthenticationProvider;
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
