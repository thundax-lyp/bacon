package com.github.thundax.bacon.common.core.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.thundax.bacon.common.core.context.BaconContextTaskDecorator;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.task.TaskDecorator;

class BaconAsyncAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withUserConfiguration(BaconAsyncAutoConfiguration.class);

    @Test
    void shouldRegisterBaconTaskExecutorAndTaskDecorator() {
        contextRunner.run(context -> {
            assertThat(context).hasBean("baconTaskExecutor");
            assertThat(context).hasBean("taskExecutor");
            assertThat(context.getBean("baconTaskExecutor")).isInstanceOf(Executor.class);
            assertThat(context.getBean("taskExecutor")).isInstanceOf(Executor.class);
            assertThat(context).hasSingleBean(TaskDecorator.class);
            assertThat(context.getBean(TaskDecorator.class)).isInstanceOf(BaconContextTaskDecorator.class);
        });
    }
}
