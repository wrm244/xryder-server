package cn.xryder.base.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;

/**
 * 全局调用虚拟线程异步配置
 *
 * @author wrm244
 */
@Configuration
public class AsyncConfig {

    @Bean("logTaskExecutor")
    public TaskExecutor taskExecutor() {
        return Thread::startVirtualThread;
    }
}