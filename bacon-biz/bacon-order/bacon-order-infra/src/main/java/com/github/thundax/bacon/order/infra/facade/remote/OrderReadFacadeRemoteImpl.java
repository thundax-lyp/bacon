package com.github.thundax.bacon.order.infra.facade.remote;

import com.github.thundax.bacon.common.core.config.RestClientFactory;
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

    public OrderReadFacadeRemoteImpl(RestClientFactory restClientFactory,
                                     @Value("${bacon.remote.order-base-url:http://127.0.0.1:8083/api}") String baseUrl) {
        this.restClient = restClientFactory.create(baseUrl);
    }

    @Override
    public OrderDetailDTO getById(Long tenantId, Long orderId) {
        // provider 查询返回内部 DTO，remote facade 不再包装，保持跨服务读取模型稳定。
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
        // 分页查询只透传当前 provider 实际支持的条件；其余筛选条件应先在契约层明确后再下沉到这里。
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
