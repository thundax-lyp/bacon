package com.github.thundax.bacon.order.infra.rpc;

import com.github.thundax.bacon.order.api.dto.OrderDetailDTO;
import com.github.thundax.bacon.order.api.dto.OrderPageQueryDTO;
import com.github.thundax.bacon.order.api.dto.OrderPageResultDTO;
import com.github.thundax.bacon.order.api.facade.OrderReadFacade;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
public class OrderReadFacadeRemoteImpl implements OrderReadFacade {

    private final RestClient restClient;

    public OrderReadFacadeRemoteImpl(@Value("${bacon.remote.order-base-url:http://localhost:8083/api}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public OrderDetailDTO getById(Long tenantId, Long orderId) {
        return restClient.get()
                .uri("/providers/orders/{orderId}?tenantId={tenantId}", orderId, tenantId)
                .retrieve()
                .body(OrderDetailDTO.class);
    }

    @Override
    public OrderDetailDTO getByOrderNo(Long tenantId, String orderNo) {
        return restClient.get()
                .uri("/providers/orders/by-order-no/{orderNo}?tenantId={tenantId}", orderNo, tenantId)
                .retrieve()
                .body(OrderDetailDTO.class);
    }

    @Override
    public OrderPageResultDTO pageOrders(OrderPageQueryDTO query) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/providers/orders")
                        .queryParam("tenantId", query.getTenantId())
                        .queryParam("userId", query.getUserId())
                        .queryParam("orderNo", query.getOrderNo())
                        .build())
                .retrieve()
                .body(OrderPageResultDTO.class);
    }
}
