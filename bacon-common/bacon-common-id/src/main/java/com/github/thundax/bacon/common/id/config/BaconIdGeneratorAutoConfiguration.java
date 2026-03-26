package com.github.thundax.bacon.common.id.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.core.IdProviderType;
import com.github.thundax.bacon.common.id.exception.IdGeneratorErrorCode;
import com.github.thundax.bacon.common.id.exception.IdGeneratorException;
import com.github.thundax.bacon.common.id.provider.LeafIdGenerator;
import com.github.thundax.bacon.common.id.provider.SnowflakeIdGenerator;
import com.github.thundax.bacon.common.id.provider.TinyIdGenerator;
import java.time.Duration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@AutoConfiguration
@EnableConfigurationProperties(BaconIdGeneratorProperties.class)
public class BaconIdGeneratorAutoConfiguration {

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public IdGenerator idGenerator(BaconIdGeneratorProperties properties,
                                   ObjectProvider<RestClient.Builder> restClientBuilderProvider,
                                   ObjectMapper objectMapper) {
        IdProviderType providerType = IdProviderType.from(properties.getProvider());
        return switch (providerType) {
            case TINYID -> createTinyIdGenerator(properties);
            case LEAF -> createLeafIdGenerator(properties, restClientBuilderProvider, objectMapper);
            case SNOWFLAKE -> createSnowflakeIdGenerator(properties);
        };
    }

    private TinyIdGenerator createTinyIdGenerator(BaconIdGeneratorProperties properties) {
        applyTinyIdProperties(properties);
        return new TinyIdGenerator();
    }

    private void applyTinyIdProperties(BaconIdGeneratorProperties properties) {
        BaconIdGeneratorProperties.TinyId tinyId = properties.getTinyId();
        if (tinyId.getServer() == null || tinyId.getServer().isBlank()) {
            throw new IdGeneratorException(IdGeneratorErrorCode.ID_PROVIDER_NOT_SUPPORTED,
                    "tinyid server is blank");
        }
        if (tinyId.getToken() == null || tinyId.getToken().isBlank()) {
            throw new IdGeneratorException(IdGeneratorErrorCode.ID_PROVIDER_NOT_SUPPORTED,
                    "tinyid token is blank");
        }
        System.setProperty("tinyid.server", tinyId.getServer());
        System.setProperty("tinyid.token", tinyId.getToken());
        System.setProperty("tinyid.connectTimeout", durationToMillisString(tinyId.getConnectTimeout(), "connectTimeout"));
        System.setProperty("tinyid.readTimeout", durationToMillisString(tinyId.getReadTimeout(), "readTimeout"));
    }

    private String durationToMillisString(Duration duration, String propertyName) {
        if (duration == null || duration.isZero() || duration.isNegative()) {
            throw new IdGeneratorException(IdGeneratorErrorCode.ID_PROVIDER_NOT_SUPPORTED,
                    "tinyid " + propertyName + " must be positive");
        }
        return String.valueOf(duration.toMillis());
    }

    private LeafIdGenerator createLeafIdGenerator(BaconIdGeneratorProperties properties,
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

    private SnowflakeIdGenerator createSnowflakeIdGenerator(BaconIdGeneratorProperties properties) {
        return new SnowflakeIdGenerator(properties.getSnowflake().getWorkerId(),
                properties.getSnowflake().getDatacenterId());
    }

    @Bean
    @ConditionalOnMissingBean
    public SnowflakeIdGenerator snowflakeIdGenerator(BaconIdGeneratorProperties properties) {
        return createSnowflakeIdGenerator(properties);
    }
}
