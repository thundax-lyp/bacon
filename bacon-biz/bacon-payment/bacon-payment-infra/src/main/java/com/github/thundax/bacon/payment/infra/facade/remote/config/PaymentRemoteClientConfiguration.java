package com.github.thundax.bacon.payment.infra.facade.remote.config;

import java.time.Duration;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
public class PaymentRemoteClientConfiguration {

    @Bean("paymentRemoteRestClient")
    public RestClient paymentRemoteRestClient(
            ObjectProvider<RestClient.Builder> restClientBuilderProvider,
            @Value("${bacon.remote.payment-base-url:http://bacon-payment-service/api}") String baseUrl,
            @Value("${bacon.remote.payment.connect-timeout:5s}") Duration connectTimeout,
            @Value("${bacon.remote.payment.read-timeout:30s}") Duration readTimeout) {
        RestClient.Builder builder = restClientBuilderProvider.getIfAvailable(RestClient::builder);
        return builder.baseUrl(baseUrl)
                .requestFactory(createRequestFactory(connectTimeout, readTimeout))
                .build();
    }

    private org.springframework.http.client.ClientHttpRequestFactory createRequestFactory(Duration connectTimeout,
                                                                                           Duration readTimeout) {
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.defaults()
                .withConnectTimeout(connectTimeout)
                .withReadTimeout(readTimeout);
        return ClientHttpRequestFactoryBuilder.detect().build(settings);
    }
}
