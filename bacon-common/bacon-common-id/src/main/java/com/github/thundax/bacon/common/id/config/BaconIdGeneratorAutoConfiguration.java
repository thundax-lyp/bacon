package com.github.thundax.bacon.common.id.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.thundax.bacon.common.core.config.RestClientFactory;
import com.github.thundax.bacon.common.id.core.DefaultIds;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.core.IdProviderType;
import com.github.thundax.bacon.common.id.core.Ids;
import com.github.thundax.bacon.common.id.event.IdFallbackAlertListener;
import com.github.thundax.bacon.common.id.exception.IdGeneratorErrorCode;
import com.github.thundax.bacon.common.id.exception.IdGeneratorException;
import com.github.thundax.bacon.common.id.provider.CompositeIdGenerator;
import com.github.thundax.bacon.common.id.provider.LeafIdGenerator;
import com.github.thundax.bacon.common.id.provider.SnowflakeIdGenerator;
import com.github.thundax.bacon.common.id.provider.TinyIdGenerator;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestClient;

@AutoConfiguration
@EnableConfigurationProperties(BaconIdGeneratorProperties.class)
public class BaconIdGeneratorAutoConfiguration {

    @Bean
    @Primary
    @ConditionalOnMissingBean
    public IdGenerator idGenerator(
            BaconIdGeneratorProperties properties,
            RestClientFactory restClientFactory,
            ObjectMapper objectMapper,
            ApplicationEventPublisher eventPublisher) {
        SnowflakeIdGenerator snowflakeIdGenerator = createSnowflakeIdGenerator(properties);
        List<IdProviderType> configuredProviders = resolveConfiguredProviders(properties);
        if (configuredProviders.size() == 1 && configuredProviders.contains(IdProviderType.SNOWFLAKE)) {
            return snowflakeIdGenerator;
        }
        List<CompositeIdGenerator.NamedIdGenerator> generators =
                resolvePrimaryGeneratorChain(properties, restClientFactory, objectMapper, configuredProviders);
        return new CompositeIdGenerator(
                generators, snowflakeIdGenerator, eventPublisher, properties.isFallbackEnabled());
    }

    private TinyIdGenerator createTinyIdGenerator(BaconIdGeneratorProperties properties) {
        applyTinyIdProperties(properties);
        return new TinyIdGenerator();
    }

    private void applyTinyIdProperties(BaconIdGeneratorProperties properties) {
        BaconIdGeneratorProperties.TinyId tinyId = properties.getTinyId();
        if (tinyId.getServer() == null || tinyId.getServer().isBlank()) {
            throw new IdGeneratorException(IdGeneratorErrorCode.ID_PROVIDER_NOT_SUPPORTED, "tinyid server is blank");
        }
        if (tinyId.getToken() == null || tinyId.getToken().isBlank()) {
            throw new IdGeneratorException(IdGeneratorErrorCode.ID_PROVIDER_NOT_SUPPORTED, "tinyid token is blank");
        }
        System.setProperty("tinyid.server", tinyId.getServer());
        System.setProperty("tinyid.token", tinyId.getToken());
        System.setProperty(
                "tinyid.connectTimeout", durationToMillisString(tinyId.getConnectTimeout(), "connectTimeout"));
        System.setProperty("tinyid.readTimeout", durationToMillisString(tinyId.getReadTimeout(), "readTimeout"));
    }

    private String durationToMillisString(Duration duration, String propertyName) {
        if (duration == null || duration.isZero() || duration.isNegative()) {
            throw new IdGeneratorException(
                    IdGeneratorErrorCode.ID_PROVIDER_NOT_SUPPORTED, "tinyid " + propertyName + " must be positive");
        }
        return String.valueOf(duration.toMillis());
    }

    private LeafIdGenerator createLeafIdGenerator(
            BaconIdGeneratorProperties properties, RestClientFactory restClientFactory, ObjectMapper objectMapper) {
        String baseUrl = properties.getLeaf().getBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IdGeneratorException(IdGeneratorErrorCode.ID_PROVIDER_NOT_SUPPORTED, "leaf base-url is blank");
        }
        RestClient restClient = restClientFactory.create(
                baseUrl,
                properties.getLeaf().getConnectTimeout(),
                properties.getLeaf().getReadTimeout());
        return new LeafIdGenerator(restClient, properties, objectMapper);
    }

    private SnowflakeIdGenerator createSnowflakeIdGenerator(BaconIdGeneratorProperties properties) {
        return new SnowflakeIdGenerator(
                properties.getSnowflake().getWorkerId(),
                properties.getSnowflake().getDatacenterId());
    }

    private List<CompositeIdGenerator.NamedIdGenerator> resolvePrimaryGeneratorChain(
            BaconIdGeneratorProperties properties,
            RestClientFactory restClientFactory,
            ObjectMapper objectMapper,
            List<IdProviderType> configuredProviders) {
        List<CompositeIdGenerator.NamedIdGenerator> generators = new ArrayList<>(configuredProviders.size());
        for (IdProviderType providerType : configuredProviders) {
            if (providerType == IdProviderType.SNOWFLAKE) {
                continue;
            }
            CompositeIdGenerator.NamedIdGenerator generator =
                    switch (providerType) {
                        case TINYID ->
                            new CompositeIdGenerator.NamedIdGenerator("tinyid", createTinyIdGenerator(properties));
                        case LEAF ->
                            new CompositeIdGenerator.NamedIdGenerator(
                                    "leaf", createLeafIdGenerator(properties, restClientFactory, objectMapper));
                        case SNOWFLAKE ->
                            throw new IllegalStateException("snowflake should not be part of primary generator chain");
                    };
            generators.add(generator);
        }
        return generators;
    }

    private List<IdProviderType> resolveConfiguredProviders(BaconIdGeneratorProperties properties) {
        LinkedHashSet<IdProviderType> providerChain = new LinkedHashSet<>();
        for (String provider : properties.resolvePrimaryProviders()) {
            providerChain.add(IdProviderType.from(provider));
        }
        return List.copyOf(providerChain);
    }

    @Bean
    @ConditionalOnMissingBean
    public SnowflakeIdGenerator snowflakeIdGenerator(BaconIdGeneratorProperties properties) {
        return createSnowflakeIdGenerator(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public Ids ids(IdGenerator idGenerator) {
        return new DefaultIds(idGenerator);
    }

    @Bean
    @ConditionalOnMissingBean
    public IdFallbackAlertListener idFallbackAlertListener() {
        return new IdFallbackAlertListener();
    }
}
