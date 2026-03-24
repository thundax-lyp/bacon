package com.github.thundax.bacon.common.core.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

class RestTemplateConfigurationTest {

    private final RestTemplateConfiguration restTemplateConfiguration = new RestTemplateConfiguration();
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RestTemplateConfiguration.class))
            .withBean(RestTemplateBuilder.class, RestTemplateBuilder::new);

    @Test
    void shouldCreateRestTemplateWithDefaultTimeouts() {
        RestTemplate restTemplate = restTemplateConfiguration.restTemplate(new RestTemplateBuilder());

        assertThat(restTemplate).isNotNull();
        assertThat(restTemplate.getRequestFactory()).isNotNull();
    }

    @Test
    void shouldCreateRestClientBuilder() {
        RestClient.Builder restClientBuilder = restTemplateConfiguration.restClientBuilder();

        assertThat(restClientBuilder).isNotNull();
        assertThat(restClientBuilder.build()).isNotNull();
    }

    @Test
    void shouldRegisterLoadBalancedClientsWhenDiscoveryEnabled() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(RestTemplate.class);
            assertThat(context).hasSingleBean(RestClient.Builder.class);
        });
    }

    @Test
    void shouldNotRegisterLoadBalancedClientsWhenDiscoveryDisabled() {
        contextRunner.withPropertyValues("spring.cloud.nacos.discovery.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(RestTemplate.class);
                    assertThat(context).doesNotHaveBean(RestClient.Builder.class);
                });
    }
}
