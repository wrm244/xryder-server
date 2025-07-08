package cn.xryder.base.config;

import cn.xryder.base.config.secrutiy.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * @Author: joetao
 * @Date: 2024/7/30 9:29
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;
    private final MyAuthenticationSuccessHandler myAuthenticationSuccessHandler;
    private final MyAuthenticationFailureHandler myAuthenticationFailureHandler;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(CustomAuthenticationEntryPoint customAuthenticationEntryPoint, JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter, MyAuthenticationSuccessHandler myAuthenticationSuccessHandler, MyAuthenticationFailureHandler myAuthenticationFailureHandler, UserDetailsService userDetailsService) {
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
        this.jwtAuthenticationTokenFilter = jwtAuthenticationTokenFilter;
        this.myAuthenticationSuccessHandler = myAuthenticationSuccessHandler;
        this.myAuthenticationFailureHandler = myAuthenticationFailureHandler;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/error", "/api/login", "/api/v1/publicKey"
                                , "/api/v1/token", "/api/v1/ai/stream"
                        )
                        .permitAll()
                        .anyRequest()
                        .authenticated()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling((exceptions) -> exceptions
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                );
        http.addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public UsernamePasswordAuthenticationFilter getCustomUsernamePasswordAuthFilter(AuthenticationManager authenticationManager) {
        UsernamePasswordAuthenticationFilter customUsernamePasswordAuthFilter = new UsernamePasswordAuthenticationFilter();
        AntPathRequestMatcher antPathRequestMatcher = new AntPathRequestMatcher("/api/login", "POST");
        customUsernamePasswordAuthFilter.setRequiresAuthenticationRequestMatcher(antPathRequestMatcher);
        customUsernamePasswordAuthFilter.setAuthenticationSuccessHandler(myAuthenticationSuccessHandler);
        customUsernamePasswordAuthFilter.setAuthenticationFailureHandler(myAuthenticationFailureHandler);
        customUsernamePasswordAuthFilter.setAuthenticationManager(authenticationManager);
        return customUsernamePasswordAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        MyUserNamePasswordAuthProvider myAuthenticationProvider = new MyUserNamePasswordAuthProvider(userDetailsService);
        myAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        // 密码是否加解密
        myAuthenticationProvider.setPasswordEncrypted(true);
        return myAuthenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
