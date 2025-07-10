package cn.xryder.base.config.secrutiy;

import cn.xryder.base.domain.R;
import cn.xryder.base.domain.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serial;
import java.io.Serializable;

/**
 * token 为空转到这里/api/v1/ai/stream
 *
 * @author wrm244
 */
@Component
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint, Serializable {

    @Serial
    private static final long serialVersionUID = -8970718410437077606L;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        log.warn("未认证访问，请求地址：{}", request.getRequestURI());
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=utf-8");
        PrintWriter printWriter = response.getWriter();
        String body = R.error(ResultCode.UNAUTHORIZED, "请先登录").toString();
        printWriter.write(body);
        printWriter.flush();
    }
}
