package top.techmczs.cuitxcpcproxy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync // 开启异步
public class ThreadPoolConfig {

    @Bean("taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);        // 核心线程
        executor.setMaxPoolSize(10);        // 最大线程
        executor.setQueueCapacity(20);      // 队列容量
        executor.setThreadNamePrefix("sse-print-task-");
        executor.initialize();
        return executor;
    }
}
