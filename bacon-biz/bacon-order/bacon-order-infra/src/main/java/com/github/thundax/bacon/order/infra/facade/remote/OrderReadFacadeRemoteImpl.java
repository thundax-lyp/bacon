package com.github.thundax.bacon.order.infra.facade.remote;

import com.github.thundax.bacon.common.core.config.RestClientFactory;
import com.github.thundax.bacon.order.api.facade.OrderReadFacade;
import com.github.thundax.bacon.order.api.request.OrderDetailFacadeRequest;
import com.github.thundax.bacon.order.api.request.OrderPageFacadeRequest;
import com.github.thundax.bacon.order.api.response.OrderDetailFacadeResponse;
import com.github.thundax.bacon.order.api.response.OrderPageFacadeResponse;
import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

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
                .uri("/providers/order/queries/detail?orderNo={orderNo}", request.getOrderNo())
                .retrieve()
                .body(OrderDetailFacadeResponse.class);
    }

    @Override
    public OrderPageFacadeResponse page(OrderPageFacadeRequest request) {
        return restClient
                .get()
                .uri(uriBuilder -> buildPageUri(uriBuilder, request))
                .retrieve()
                .body(OrderPageFacadeResponse.class);
    }

    private URI buildPageUri(UriBuilder uriBuilder, OrderPageFacadeRequest request) {
        UriBuilder builder = uriBuilder.path("/providers/order/queries/page");
        builder = queryParamIfPresent(builder, "userId", request.getUserId());
        builder = queryParamIfPresent(builder, "orderNo", request.getOrderNo());
        builder = queryParamIfPresent(builder, "orderStatus", request.getOrderStatus());
        builder = queryParamIfPresent(builder, "payStatus", request.getPayStatus());
        builder = queryParamIfPresent(builder, "inventoryStatus", request.getInventoryStatus());
        builder = queryParamIfPresent(builder, "createdAtFrom", request.getCreatedAtFrom());
        builder = queryParamIfPresent(builder, "createdAtTo", request.getCreatedAtTo());
        builder = queryParamIfPresent(builder, "pageNo", request.getPageNo());
        builder = queryParamIfPresent(builder, "pageSize", request.getPageSize());
        return builder.build();
    }

    private UriBuilder queryParamIfPresent(UriBuilder builder, String name, Object value) {
        return value == null ? builder : builder.queryParam(name, value);
    }
}
