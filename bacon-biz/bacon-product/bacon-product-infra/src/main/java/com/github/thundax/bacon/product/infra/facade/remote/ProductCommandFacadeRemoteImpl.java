package com.github.thundax.bacon.product.infra.facade.remote;

import com.github.thundax.bacon.common.core.config.RestClientFactory;
import com.github.thundax.bacon.product.api.dto.ProductSnapshotDTO;
import com.github.thundax.bacon.product.api.facade.ProductCommandFacade;
import com.github.thundax.bacon.product.api.request.ProductOrderSnapshotCreateFacadeRequest;
import com.github.thundax.bacon.product.api.response.ProductOrderSnapshotCreateFacadeResponse;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
public class ProductCommandFacadeRemoteImpl implements ProductCommandFacade {

    private static final String PROVIDER_TOKEN_HEADER = "X-Bacon-Provider-Token";

    private final RestClient restClient;

    public ProductCommandFacadeRemoteImpl(
            RestClientFactory restClientFactory,
            @Value("${bacon.remote.product-base-url:http://bacon-product-service/api}") String baseUrl,
            @Value("${bacon.remote.product.provider-token:}") String providerToken) {
        this.restClient = restClientFactory.create(baseUrl, PROVIDER_TOKEN_HEADER, providerToken);
    }

    @Override
    public ProductOrderSnapshotCreateFacadeResponse createOrderProductSnapshot(
            ProductOrderSnapshotCreateFacadeRequest request) {
        ProductSnapshotDTO snapshot = restClient
                .post()
                .uri("/providers/product/commands/create-order-product-snapshot?tenantId={tenantId}", request.tenantId())
                .body(Map.of(
                        "orderNo", request.orderNo(),
                        "orderItemNo", request.orderItemNo(),
                        "skuId", request.skuId(),
                        "quantity", request.quantity()))
                .retrieve()
                .body(ProductSnapshotDTO.class);
        return new ProductOrderSnapshotCreateFacadeResponse(snapshot);
    }
}
