package cn.xryder.base.config.secrutiy;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Map;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * 支持JSON格式登录的自定义认证过滤器
 * 从请求body中读取JSON格式的username和password
 * 
 * @author wrm244
 */
@Slf4j
public class JsonUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonUsernamePasswordAuthenticationFilter() {
        super();
        // 设置登录路径和请求方法
        setRequiresAuthenticationRequestMatcher(
                request -> "/api/login".equals(request.getRequestURI()) && "POST".equals(request.getMethod()));
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {

        if (!request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }

        String username;
        String password;

        // 检查Content-Type是否为JSON
        String contentType = request.getContentType();
        if (contentType != null && contentType.contains("application/json")) {
            // 从JSON body中读取用户名和密码
            try {
                TypeReference<Map<String, String>> typeRef = new TypeReference<Map<String, String>>() {
                };
                Map<String, String> loginData = objectMapper.readValue(request.getInputStream(), typeRef);
                username = loginData.get("username");
                password = loginData.get("password");

                log.debug("从JSON body中读取到用户名: {}", username);
            } catch (IOException e) {
                log.error("解析JSON登录数据失败", e);
                throw new AuthenticationServiceException("Invalid JSON format", e);
            }
        } else {
            // 兼容form表单提交方式
            username = obtainUsername(request);
            password = obtainPassword(request);
            log.debug("从表单参数中读取到用户名: {}", username);
        }

        if (username == null) {
            username = "";
        }

        if (password == null) {
            password = "";
        }

        username = username.trim();

        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);

        // 设置详细信息
        setDetails(request, authRequest);

        return this.getAuthenticationManager().authenticate(authRequest);
    }
}
