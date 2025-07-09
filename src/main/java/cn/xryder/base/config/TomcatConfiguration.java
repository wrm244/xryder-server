package cn.xryder.base.config;

import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.Executors;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * Tomcat 配置
 *
 * @author wrm244
 */
@Configuration
public class TomcatConfiguration {

    @Bean
    TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutorCustomizer() {

        return protocolHandler -> {
            // 使用虚拟线程来处理每一个请求
            protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        };
    }

    @Bean
    public RouterFunction<ServerResponse> sseRoutes() {
        return route(POST("/api/v1/ai/stream"), request ->
                ServerResponse.ok()
                        .contentType(MediaType.TEXT_EVENT_STREAM)
                        .body(Flux.interval(Duration.ofSeconds(15)).map(seq -> "heartbeat " + seq), String.class)
                        .timeout(Duration.ofSeconds(60))  // 超时时间设置为 60 秒
        );
    }
}