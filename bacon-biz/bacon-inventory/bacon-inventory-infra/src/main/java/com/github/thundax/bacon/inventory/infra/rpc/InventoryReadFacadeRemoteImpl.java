package com.github.thundax.bacon.inventory.infra.rpc;

import com.github.thundax.bacon.inventory.api.dto.InventoryReservationDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryStockDTO;
import com.github.thundax.bacon.inventory.api.facade.InventoryReadFacade;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Set;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
public class InventoryReadFacadeRemoteImpl implements InventoryReadFacade {

    private static final ParameterizedTypeReference<List<InventoryStockDTO>> LIST_TYPE =
            new ParameterizedTypeReference<>() { };

    private final RestClient restClient;

    public InventoryReadFacadeRemoteImpl(@Value("${bacon.remote.inventory-base-url:http://localhost:8084/api}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public InventoryStockDTO getAvailableStock(Long tenantId, Long skuId) {
        return restClient.get()
                .uri("/providers/inventory/stocks/{skuId}?tenantId={tenantId}", skuId, tenantId)
                .retrieve()
                .body(InventoryStockDTO.class);
    }

    @Override
    public List<InventoryStockDTO> batchGetAvailableStock(Long tenantId, Set<Long> skuIds) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/providers/inventory/stocks")
                        .queryParam("tenantId", tenantId)
                        .queryParam("skuIds", skuIds.toArray())
                        .build())
                .retrieve()
                .body(LIST_TYPE);
    }

    @Override
    public InventoryReservationDTO getReservationByOrderNo(Long tenantId, String orderNo) {
        return restClient.get()
                .uri("/providers/inventory/reservations/{orderNo}?tenantId={tenantId}", orderNo, tenantId)
                .retrieve()
                .body(InventoryReservationDTO.class);
    }
}
