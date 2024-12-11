package cn.xryder.base;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.Executors;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * @Author: joetao
 * @Date: 2024/8/1 14:57
 * @Email: cutesimba@163.com
 */
@SpringBootApplication
public class XryderServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(XryderServerApplication.class, args);
    }

    // 使用虚拟线程处理请求
    @Bean
    public TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutorCustomizer() {
        return protocolHandler -> {
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
