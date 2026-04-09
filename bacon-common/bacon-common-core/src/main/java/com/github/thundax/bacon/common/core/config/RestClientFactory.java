package com.github.thundax.bacon.common.core.config;

import java.time.Duration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

/**
 * 统一同步 HTTP 客户端创建入口，避免业务模块重复拼装超时与请求工厂。
 */
public class RestClientFactory {

    private final ObjectProvider<RestClient.Builder> restClientBuilderProvider;

    public RestClientFactory(ObjectProvider<RestClient.Builder> restClientBuilderProvider) {
        this.restClientBuilderProvider = restClientBuilderProvider;
    }

    public RestClient create(String baseUrl) {
        return builder().baseUrl(baseUrl).build();
    }

    public RestClient create(String baseUrl, String headerName, String headerValue) {
        return applyDefaultHeader(builder().baseUrl(baseUrl), headerName, headerValue)
                .build();
    }

    public RestClient create(String baseUrl, Duration connectTimeout, Duration readTimeout) {
        return builder()
                .baseUrl(baseUrl)
                .requestFactory(createRequestFactory(connectTimeout, readTimeout))
                .build();
    }

    public RestClient create(
            String baseUrl, Duration connectTimeout, Duration readTimeout, String headerName, String headerValue) {
        return applyDefaultHeader(
                        builder().baseUrl(baseUrl).requestFactory(createRequestFactory(connectTimeout, readTimeout)),
                        headerName,
                        headerValue)
                .build();
    }

    public ClientHttpRequestFactory createRequestFactory(Duration connectTimeout, Duration readTimeout) {
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.defaults()
                .withConnectTimeout(connectTimeout)
                .withReadTimeout(readTimeout);
        return ClientHttpRequestFactoryBuilder.detect().build(settings);
    }

    private RestClient.Builder builder() {
        return restClientBuilderProvider.getIfAvailable(RestClient::builder);
    }

    private RestClient.Builder applyDefaultHeader(RestClient.Builder builder, String headerName, String headerValue) {
        if (StringUtils.hasText(headerName) && StringUtils.hasText(headerValue)) {
            builder.defaultHeader(headerName, headerValue.trim());
        }
        return builder;
    }
}
