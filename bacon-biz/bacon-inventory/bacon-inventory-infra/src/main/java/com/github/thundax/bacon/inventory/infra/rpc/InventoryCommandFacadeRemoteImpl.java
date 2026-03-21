package com.github.thundax.bacon.inventory.infra.rpc;

import com.github.thundax.bacon.inventory.api.dto.InventoryReservationItemDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationResultDTO;
import com.github.thundax.bacon.inventory.api.facade.InventoryCommandFacade;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
public class InventoryCommandFacadeRemoteImpl implements InventoryCommandFacade {

    private final RestClient restClient;

    public InventoryCommandFacadeRemoteImpl(@Value("${bacon.remote.inventory-base-url:http://localhost:8085}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public InventoryReservationResultDTO reserveStock(Long tenantId, String orderNo, List<InventoryReservationItemDTO> items) {
        return restClient.post()
                .uri("/providers/inventory/reservations/{orderNo}/reserve?tenantId={tenantId}", orderNo, tenantId)
                .body(items)
                .retrieve()
                .body(InventoryReservationResultDTO.class);
    }

    @Override
    public InventoryReservationResultDTO releaseReservedStock(Long tenantId, String orderNo, String reason) {
        return restClient.post()
                .uri("/providers/inventory/reservations/{orderNo}/release?tenantId={tenantId}&reason={reason}",
                        orderNo, tenantId, reason)
                .retrieve()
                .body(InventoryReservationResultDTO.class);
    }

    @Override
    public InventoryReservationResultDTO deductReservedStock(Long tenantId, String orderNo) {
        return restClient.post()
                .uri("/providers/inventory/reservations/{orderNo}/deduct?tenantId={tenantId}", orderNo, tenantId)
                .retrieve()
                .body(InventoryReservationResultDTO.class);
    }
}
