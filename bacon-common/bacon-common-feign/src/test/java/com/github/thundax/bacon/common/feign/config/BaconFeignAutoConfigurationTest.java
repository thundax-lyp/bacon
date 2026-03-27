package com.github.thundax.bacon.common.feign.config;

import static org.assertj.core.api.Assertions.assertThat;

import feign.Logger;
import feign.Retryer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class BaconFeignAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(BaconFeignAutoConfiguration.class));

    @Test
    void shouldUseDefaultFeignGovernanceBeans() {
        contextRunner.run(context -> {
            assertThat(context.getBean(Logger.Level.class)).isEqualTo(Logger.Level.BASIC);
            assertThat(context.getBean(Retryer.class)).isSameAs(Retryer.NEVER_RETRY);
        });
    }

    @Test
    void shouldAllowRetryAndLoggerOverrideByProperties() {
        contextRunner
                .withPropertyValues("bacon.feign.logger-level=full", "bacon.feign.retry-enabled=true")
                .run(context -> {
                    assertThat(context.getBean(Logger.Level.class)).isEqualTo(Logger.Level.FULL);
                    assertThat(context.getBean(Retryer.class)).isNotSameAs(Retryer.NEVER_RETRY);
                });
    }
}
