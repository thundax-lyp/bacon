package com.github.thundax.bacon.product.infra.facade.remote;

import com.github.thundax.bacon.common.core.config.RestClientFactory;
import com.github.thundax.bacon.product.api.dto.ProductSkuSaleInfoDTO;
import com.github.thundax.bacon.product.api.facade.ProductReadFacade;
import com.github.thundax.bacon.product.api.request.ProductSkuSaleInfoFacadeRequest;
import com.github.thundax.bacon.product.api.response.ProductSkuSaleInfoFacadeResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
public class ProductReadFacadeRemoteImpl implements ProductReadFacade {

    private static final String PROVIDER_TOKEN_HEADER = "X-Bacon-Provider-Token";

    private final RestClient restClient;

    public ProductReadFacadeRemoteImpl(
            RestClientFactory restClientFactory,
            @Value("${bacon.remote.product-base-url:http://bacon-product-service/api}") String baseUrl,
            @Value("${bacon.remote.product.provider-token:}") String providerToken) {
        this.restClient = restClientFactory.create(baseUrl, PROVIDER_TOKEN_HEADER, providerToken);
    }

    @Override
    public ProductSkuSaleInfoFacadeResponse getSkuSaleInfo(ProductSkuSaleInfoFacadeRequest request) {
        ProductSkuSaleInfoDTO skuSaleInfo = restClient
                .get()
                .uri(
                        "/providers/product/queries/sku-sale-info?tenantId={tenantId}&skuId={skuId}",
                        request.tenantId(),
                        request.skuId())
                .retrieve()
                .body(ProductSkuSaleInfoDTO.class);
        return new ProductSkuSaleInfoFacadeResponse(skuSaleInfo);
    }
}
