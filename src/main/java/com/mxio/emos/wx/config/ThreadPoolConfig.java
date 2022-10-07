package com.mxio.emos.wx.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;
/**
 * @author mxio
 */

@Configuration
public class ThreadPoolConfig {

    /**运行一次之后，类交给spring管理*/
    @Bean("AsyncTaskExecutor")
    public AsyncTaskExecutor taskExecutor() {
        //AsyncTaskExecutor是ThreadPoolTaskExecutor的父类，ThreadPoolTaskExecutor是其子类
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 设置核心线程数
        executor.setCorePoolSize(6);
        // 设置最大线程数
        executor.setMaxPoolSize(12);
        // 设置队列容量
        executor.setQueueCapacity(32);
        // 设置线程活跃时间（秒）
        executor.setKeepAliveSeconds(60);
        // 设置默认线程名称
        executor.setThreadNamePrefix("task-");
        // 设置拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
