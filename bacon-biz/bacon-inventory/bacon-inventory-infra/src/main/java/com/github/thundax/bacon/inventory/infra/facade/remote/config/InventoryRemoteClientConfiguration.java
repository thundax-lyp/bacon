package com.github.thundax.bacon.inventory.infra.facade.remote.config;

import com.github.thundax.bacon.common.core.config.RestClientFactory;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
public class InventoryRemoteClientConfiguration {

    private static final String PROVIDER_TOKEN_HEADER = "X-Bacon-Provider-Token";

    @Bean("inventoryRemoteRestClient")
    public RestClient inventoryRemoteRestClient(
            RestClientFactory restClientFactory,
            @Value("${bacon.remote.inventory-base-url:http://bacon-inventory-service/api}") String baseUrl,
            @Value("${bacon.remote.inventory.connect-timeout:5s}") Duration connectTimeout,
            @Value("${bacon.remote.inventory.read-timeout:30s}") Duration readTimeout,
            @Value("${bacon.remote.inventory.provider-token:}") String providerToken) {
        return restClientFactory.create(baseUrl, connectTimeout, readTimeout, PROVIDER_TOKEN_HEADER, providerToken);
    }
}
