package com.github.thundax.bacon.order.infra.rpc;

import com.github.thundax.bacon.order.api.dto.OrderSummaryDTO;
import com.github.thundax.bacon.order.api.facade.OrderReadFacade;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
public class OrderReadFacadeRemoteImpl implements OrderReadFacade {

    private final RestClient restClient;

    public OrderReadFacadeRemoteImpl(@Value("${bacon.remote.order-base-url:http://localhost:8081}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public OrderSummaryDTO getById(Long orderId) {
        return restClient.get()
                .uri("/providers/orders/{orderId}", orderId)
                .retrieve()
                .body(OrderSummaryDTO.class);
    }
}
