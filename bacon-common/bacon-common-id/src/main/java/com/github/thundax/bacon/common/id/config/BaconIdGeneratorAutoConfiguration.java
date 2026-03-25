package com.github.thundax.bacon.common.id.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.core.IdProviderType;
import com.github.thundax.bacon.common.id.exception.IdGeneratorErrorCode;
import com.github.thundax.bacon.common.id.exception.IdGeneratorException;
import com.github.thundax.bacon.common.id.provider.LeafIdGenerator;
import com.github.thundax.bacon.common.id.provider.TinyIdGenerator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@AutoConfiguration
@EnableConfigurationProperties(BaconIdGeneratorProperties.class)
public class BaconIdGeneratorAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public IdGenerator idGenerator(BaconIdGeneratorProperties properties,
                                   TinyIdGenerator tinyIdGenerator,
                                   LeafIdGenerator leafIdGenerator) {
        IdProviderType providerType = IdProviderType.from(properties.getProvider());
        return switch (providerType) {
            case TINYID -> tinyIdGenerator;
            case LEAF -> leafIdGenerator;
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public TinyIdGenerator tinyIdGenerator() {
        return new TinyIdGenerator();
    }

    @Bean
    @ConditionalOnMissingBean
    public LeafIdGenerator leafIdGenerator(BaconIdGeneratorProperties properties,
                                           ObjectProvider<RestClient.Builder> restClientBuilderProvider,
                                           ObjectMapper objectMapper) {
        String baseUrl = properties.getLeaf().getBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IdGeneratorException(IdGeneratorErrorCode.ID_PROVIDER_NOT_SUPPORTED,
                    "leaf base-url is blank");
        }
        RestClient.Builder builder = restClientBuilderProvider.getIfAvailable(RestClient::builder);
        ClientHttpRequestFactory requestFactory = createRequestFactory(properties);
        RestClient restClient = builder.baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
        return new LeafIdGenerator(restClient, properties, objectMapper);
    }

    private ClientHttpRequestFactory createRequestFactory(BaconIdGeneratorProperties properties) {
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.defaults()
                .withConnectTimeout(properties.getLeaf().getConnectTimeout())
                .withReadTimeout(properties.getLeaf().getReadTimeout());
        return ClientHttpRequestFactoryBuilder.detect().build(settings);
    }
}
