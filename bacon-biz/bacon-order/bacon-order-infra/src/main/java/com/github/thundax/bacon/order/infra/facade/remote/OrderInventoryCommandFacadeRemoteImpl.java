package com.github.thundax.bacon.order.infra.facade.remote;

import com.github.thundax.bacon.inventory.api.dto.InventoryReservationItemDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationResultDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReserveCommandDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReleaseCommandDTO;
import com.github.thundax.bacon.inventory.api.facade.InventoryCommandFacade;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
public class OrderInventoryCommandFacadeRemoteImpl implements InventoryCommandFacade {

    private final RestClient restClient;

    public OrderInventoryCommandFacadeRemoteImpl(
            @Value("${bacon.remote.inventory-base-url:http://127.0.0.1:8084/api}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public InventoryReservationResultDTO reserveStock(Long tenantId, String orderNo, List<InventoryReservationItemDTO> items) {
        return restClient.post()
                .uri("/providers/inventory/reservations/{orderNo}/reserve?tenantId={tenantId}", orderNo, tenantId)
                .body(new InventoryReserveCommandDTO(items))
                .retrieve()
                .body(InventoryReservationResultDTO.class);
    }

    @Override
    public InventoryReservationResultDTO releaseReservedStock(Long tenantId, String orderNo, String reason) {
        return restClient.post()
                .uri("/providers/inventory/reservations/{orderNo}/release?tenantId={tenantId}", orderNo, tenantId)
                .body(new InventoryReleaseCommandDTO(reason))
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
