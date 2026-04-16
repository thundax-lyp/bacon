package com.github.thundax.bacon.order.infra.facade.remote;

import com.github.thundax.bacon.common.core.config.RestClientFactory;
import com.github.thundax.bacon.order.api.facade.OrderReadFacade;
import com.github.thundax.bacon.order.api.request.OrderDetailFacadeRequest;
import com.github.thundax.bacon.order.api.request.OrderPageFacadeRequest;
import com.github.thundax.bacon.order.api.response.OrderDetailFacadeResponse;
import com.github.thundax.bacon.order.api.response.OrderPageFacadeResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
public class OrderReadFacadeRemoteImpl implements OrderReadFacade {

    private static final String PROVIDER_TOKEN_HEADER = "X-Bacon-Provider-Token";

    private final RestClient restClient;

    public OrderReadFacadeRemoteImpl(
            RestClientFactory restClientFactory,
            @Value("${bacon.remote.order-base-url:http://bacon-order-service/api}") String baseUrl,
            @Value("${bacon.remote.order.provider-token:}") String providerToken) {
        this.restClient = restClientFactory.create(baseUrl, PROVIDER_TOKEN_HEADER, providerToken);
    }

    @Override
    public OrderDetailFacadeResponse getByOrderNo(OrderDetailFacadeRequest request) {
        return restClient
                .get()
                .uri("/providers/order/{orderNo}", request.getOrderNo())
                .retrieve()
                .body(OrderDetailFacadeResponse.class);
    }

    @Override
    public OrderPageFacadeResponse pageOrders(OrderPageFacadeRequest request) {
        // 分页查询只透传当前 provider 实际支持的条件；其余筛选条件应先在契约层明确后再下沉到这里。
        return restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/providers/order")
                        .queryParam("userId", request.getUserId())
                        .queryParam("orderNo", request.getOrderNo())
                        .build())
                .retrieve()
                .body(OrderPageFacadeResponse.class);
    }
}
