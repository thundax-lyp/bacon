package com.github.thundax.bacon.common.core.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.context.BaconContextHolder.BaconContext;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

class RestTemplateAutoConfigurationTest {

    private final RestTemplateAutoConfiguration restTemplateAutoConfiguration = new RestTemplateAutoConfiguration();
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(RestTemplateAutoConfiguration.class))
            .withBean(RestTemplateBuilder.class, RestTemplateBuilder::new);

    @Test
    void shouldCreateRestTemplateWithDefaultTimeouts() {
        RestTemplate restTemplate = restTemplateAutoConfiguration.restTemplate(new RestTemplateBuilder());

        assertThat(restTemplate).isNotNull();
        assertThat(restTemplate.getRequestFactory()).isNotNull();
    }

    @Test
    void shouldCreateRestClientBuilder() {
        RestClient.Builder restClientBuilder = restTemplateAutoConfiguration.restClientBuilder();

        assertThat(restClientBuilder).isNotNull();
        assertThat(restClientBuilder.build()).isNotNull();
    }

    @Test
    void shouldApplyTenantIdAndUserIdHeadersFromContext() {
        HttpHeaders headers = new HttpHeaders();
        BaconContextHolder.set(new BaconContext(1001L, 2001L));
        try {
            RestTemplateAutoConfiguration.applyContextHeaders(headers);
        } finally {
            BaconContextHolder.clear();
        }

        assertThat(headers.getFirst("X-Tenant-Id")).isEqualTo("1001");
        assertThat(headers.getFirst("X-User-Id")).isEqualTo("2001");
    }

    @Test
    void shouldCreateRestClientFactory() {
        contextRunner.run(context -> {
            RestClientFactory restClientFactory = context.getBean(RestClientFactory.class);
            assertThat(restClientFactory).isNotNull();
            assertThat(restClientFactory.create("http://localhost")).isNotNull();
        });
    }

    @Test
    void shouldRegisterLoadBalancedClientsWhenDiscoveryEnabled() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(RestTemplate.class);
            assertThat(context).hasSingleBean(RestClient.Builder.class);
            assertThat(context).hasSingleBean(RestClientFactory.class);
        });
    }

    @Test
    void shouldNotRegisterLoadBalancedClientsWhenDiscoveryDisabled() {
        contextRunner
                .withPropertyValues("spring.cloud.nacos.discovery.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(RestTemplate.class);
                    assertThat(context).doesNotHaveBean(RestClient.Builder.class);
                    assertThat(context).hasSingleBean(RestClientFactory.class);
                });
    }
}
