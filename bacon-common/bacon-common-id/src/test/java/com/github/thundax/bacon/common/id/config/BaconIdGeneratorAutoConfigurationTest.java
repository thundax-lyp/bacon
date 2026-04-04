package com.github.thundax.bacon.common.id.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.thundax.bacon.common.core.config.RestClientFactory;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.provider.CompositeIdGenerator;
import com.github.thundax.bacon.common.id.provider.SnowflakeIdGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class BaconIdGeneratorAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(BaconIdGeneratorAutoConfiguration.class))
            .withBean(RestClientFactory.class, () -> new RestClientFactory(null))
            .withBean(ObjectMapper.class, ObjectMapper::new);

    @Test
    void shouldWrapSingleRemoteProviderWithSnowflakeFallback() {
        contextRunner
                .withPropertyValues(
                        "bacon.id.generator.provider=tinyid",
                        "bacon.id.generator.tiny-id.server=127.0.0.1:9999",
                        "bacon.id.generator.tiny-id.token=test-token")
                .run(context -> assertThat(context.getBean(IdGenerator.class)).isInstanceOf(CompositeIdGenerator.class));
    }

    @Test
    void shouldUseCompositeGeneratorWhenProviderListConfigured() {
        contextRunner
                .withPropertyValues(
                        "bacon.id.generator.providers[0]=tinyid",
                        "bacon.id.generator.providers[1]=snowflake",
                        "bacon.id.generator.tiny-id.server=127.0.0.1:9999",
                        "bacon.id.generator.tiny-id.token=test-token")
                .run(context -> assertThat(context.getBean(IdGenerator.class)).isInstanceOf(CompositeIdGenerator.class));
    }

    @Test
    void shouldUseSnowflakeDirectlyWhenNoRemoteProviderConfigured() {
        contextRunner
                .withPropertyValues("bacon.id.generator.provider=snowflake")
                .run(context -> assertThat(context.getBean(IdGenerator.class)).isInstanceOf(SnowflakeIdGenerator.class));
    }

    @Test
    void shouldUseCompositeGeneratorWhenFallbackDisabled() {
        contextRunner
                .withPropertyValues(
                        "bacon.id.generator.providers[0]=tinyid",
                        "bacon.id.generator.fallback-enabled=false",
                        "bacon.id.generator.tiny-id.server=127.0.0.1:9999",
                        "bacon.id.generator.tiny-id.token=test-token")
                .run(context -> assertThat(context.getBean(IdGenerator.class)).isInstanceOf(CompositeIdGenerator.class));
    }
}
