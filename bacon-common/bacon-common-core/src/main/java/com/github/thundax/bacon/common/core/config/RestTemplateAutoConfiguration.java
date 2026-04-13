package com.github.thundax.bacon.common.core.config;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import java.time.Duration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

/**
 * 创建全局 RestTemplate 配置，用于统一项目内同步 HTTP 调用的默认超时设置。
 */
@AutoConfiguration
@ConditionalOnClass({RestTemplate.class, RestClient.class, LoadBalanced.class})
public class RestTemplateAutoConfiguration {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(30);
    private static final String TENANT_ID_HEADER = "X-Tenant-Id";
    private static final String USER_ID_HEADER = "X-User-Id";

    /**
     * 创建 RestTemplate Bean，供业务代码发起 HTTP 请求时复用统一的连接与读取超时配置。
     */
    @Bean
    @LoadBalanced
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "spring.cloud.nacos.discovery.enabled", havingValue = "true", matchIfMissing = true)
    public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .connectTimeout(CONNECT_TIMEOUT)
                .readTimeout(READ_TIMEOUT)
                .build();
    }

    /**
     * 创建 RestClient.Builder Bean，供业务代码基于统一的默认超时配置构建声明式 HTTP 客户端。
     */
    @Bean
    @LoadBalanced
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "spring.cloud.nacos.discovery.enabled", havingValue = "true", matchIfMissing = true)
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder()
                .requestFactory(createRequestFactory())
                .requestInterceptor((request, body, execution) -> {
                    applyContextHeaders(request.getHeaders());
                    return execution.execute(request, body);
                });
    }

    @Bean
    public RestClientFactory restClientFactory(ObjectProvider<RestClient.Builder> restClientBuilderProvider) {
        return new RestClientFactory(restClientBuilderProvider);
    }

    private ClientHttpRequestFactory createRequestFactory() {
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.defaults()
                .withConnectTimeout(CONNECT_TIMEOUT)
                .withReadTimeout(READ_TIMEOUT);
        return ClientHttpRequestFactoryBuilder.detect().build(settings);
    }

    static void applyContextHeaders(HttpHeaders headers) {
        Long tenantId = BaconContextHolder.currentTenantId();
        if (tenantId != null) {
            headers.set(TENANT_ID_HEADER, String.valueOf(tenantId));
        }
        Long userId = BaconContextHolder.currentUserId();
        if (userId != null) {
            headers.set(USER_ID_HEADER, String.valueOf(userId));
        }
    }
}
