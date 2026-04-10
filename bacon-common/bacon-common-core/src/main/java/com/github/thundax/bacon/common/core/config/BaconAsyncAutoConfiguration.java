package com.github.thundax.bacon.common.core.config;

import com.github.thundax.bacon.common.core.context.BaconContextTaskDecorator;
import java.util.concurrent.Executor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@AutoConfiguration
@ConditionalOnClass(ThreadPoolTaskExecutor.class)
@EnableAsync
public class BaconAsyncAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(TaskDecorator.class)
    public TaskDecorator baconContextTaskDecorator() {
        return new BaconContextTaskDecorator();
    }

    @Bean(name = {"baconTaskExecutor", "taskExecutor"})
    @ConditionalOnMissingBean(name = {"baconTaskExecutor", "taskExecutor"})
    public Executor baconTaskExecutor(TaskDecorator taskDecorator) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("bacon-async-");
        executor.setCorePoolSize(Math.max(2, Runtime.getRuntime().availableProcessors()));
        executor.setMaxPoolSize(Math.max(4, Runtime.getRuntime().availableProcessors() * 2));
        executor.setQueueCapacity(200);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.setTaskDecorator(taskDecorator);
        executor.initialize();
        return executor;
    }
}
