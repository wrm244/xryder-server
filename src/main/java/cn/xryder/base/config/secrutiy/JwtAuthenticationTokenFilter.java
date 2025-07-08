package cn.xryder.base.config.secrutiy;

import cn.xryder.base.domain.ResultCode;
import cn.xryder.base.domain.ResultJson;
import cn.xryder.base.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
 * token校验过滤器
 * 当登录成功后，每次请求携带token进行访问，该过滤器会获取token，并从token解析出用户信息
 * 成功解析出用户信息后设置SecurityContextHolder为登录状态
 *
 * @author: JoeTao
 * createAt: 2018/9/14
 */
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    public JwtAuthenticationTokenFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        //校验token， 并将token解析出的用户信息保存到SecurityContextHolder中
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;
        Boolean isAccessToken = false;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                username = jwtService.extractUsername(token);
                isAccessToken = jwtService.isAccessToken(token);
            } catch (ExpiredJwtException e) {
                returnExpiredTokenException(response);
                return;
            }
        }

        if (isAccessToken && username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            Set<GrantedAuthority> authoritiesFromToken = jwtService.getAuthoritiesFromToken(token);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, null, authoritiesFromToken);
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        chain.doFilter(request, response);
    }

    private void returnExpiredTokenException(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        PrintWriter printWriter = response.getWriter();
        String body = ResultJson.failure(ResultCode.TOKEN_EXPIRED).toString();
        printWriter.write(body);
        printWriter.flush();
    }
}
