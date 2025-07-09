package cn.xryder.base.config.secrutiy;

import cn.xryder.base.domain.R;
import cn.xryder.base.domain.ResultCode;
import cn.xryder.base.service.JwtService;
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

/**
 * JWT认证过滤器 - 优化版本
 * 当登录成功后，每次请求携带token进行访问，该过滤器会获取token，并从token解析出用户信息
 * 成功解析出用户信息后设置SecurityContextHolder为登录状态
 *
 * @author wrm244
 */
@Slf4j
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final JwtService jwtService;

    public JwtAuthenticationTokenFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain chain) throws ServletException, IOException {

        String token = extractTokenFromRequest(request);

        if (StringUtils.isNotBlank(token)) {
            TokenValidationResult result = validateToken(token);

            if (result == TokenValidationResult.EXPIRED) {
                // Token过期 - 返回特定的过期响应
                returnExpiredTokenException(response);
                return;
            } else if (result == TokenValidationResult.INVALID) {
                // Token无效 - 返回无效Token响应
                returnInvalidTokenException(response);
                return;
            } else if (result == TokenValidationResult.VALID) {
                // Token有效 - 设置认证信息
                authenticateToken(request, token);
            }
            // 如果是 NO_TOKEN 或其他情况，继续执行链，让 AuthenticationEntryPoint 处理
        }

        chain.doFilter(request, response);
    }

    /**
     * Token验证结果枚举
     */
    private enum TokenValidationResult {
        VALID, // 有效
        EXPIRED, // 过期
        INVALID, // 无效
        NO_TOKEN // 无Token
    }

    /**
     * 验证Token状态 - 修复版本，正确处理过期Token
     */
    private TokenValidationResult validateToken(String token) {
        try {
            // 使用安全方法提取用户名，即使Token过期也能提取
            String username = jwtService.extractUsernameFromExpiredToken(token);
            if (StringUtils.isBlank(username)) {
                log.debug("无法从Token中提取用户名");
                return TokenValidationResult.INVALID;
            }

            // 使用安全方法检查Token是否过期
            if (jwtService.isTokenExpiredSafely(token)) {
                log.debug("Token已过期，用户: {}", username);
                return TokenValidationResult.EXPIRED;
            }

            // 检查Token类型和有效性
            String tokenType = jwtService.getTokenTypeSafely(token);
            if (JwtService.ACCESS_TOKEN.equals(tokenType) || JwtService.AI_ACCESS_TOKEN.equals(tokenType)) {
                log.debug("Token有效，用户: {}, 类型: {}", username, tokenType);
                return TokenValidationResult.VALID;
            } else {
                log.debug("Token类型无效，用户: {}, 类型: {}", username, tokenType);
                return TokenValidationResult.INVALID;
            }
        } catch (Exception e) {
            // 其他未知异常
            log.warn("Token验证异常: {}", e.getMessage());
            return TokenValidationResult.INVALID;
        }
    }

    /**
     * 从请求中提取Token
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    /**
     * 验证并处理Token - 简化版本
     */
    private void authenticateToken(HttpServletRequest request, String token) {
        // 检查是否已经认证过
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return;
        }

        String username = jwtService.extractUsername(token);
        if (StringUtils.isNotBlank(username)) {
            Set<GrantedAuthority> authorities = jwtService.getAuthoritiesFromToken(token);

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username,
                    null, authorities);
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            log.debug("用户 {} 认证成功", username);
        }
    }

    /**
     * 返回Token过期异常响应
     */
    private void returnExpiredTokenException(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");

        PrintWriter printWriter = response.getWriter();
        String body = R.error(ResultCode.TOKEN_EXPIRED, "访问令牌已过期，请刷新token").toString();
        printWriter.write(body);
        printWriter.flush();

        log.info("返回Token过期响应");
    }

    /**
     * 返回Token无效异常响应
     */
    private void returnInvalidTokenException(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");

        PrintWriter printWriter = response.getWriter();
        String body = R.error(ResultCode.UNAUTHORIZED, "访问令牌无效，请重新登录").toString();
        printWriter.write(body);
        printWriter.flush();

        log.debug("返回Token无效响应");
    }
}
