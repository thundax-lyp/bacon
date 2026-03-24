package com.github.thundax.bacon.common.core.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

class RestTemplateConfigurationTest {

    private final RestTemplateConfiguration restTemplateConfiguration = new RestTemplateConfiguration();

    @Test
    void shouldCreateRestTemplateWithDefaultTimeouts() {
        RestTemplate restTemplate = restTemplateConfiguration.restTemplate(new RestTemplateBuilder());

        assertThat(restTemplate).isNotNull();
        assertThat(restTemplate.getRequestFactory()).isNotNull();
    }
}
