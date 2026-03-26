package com.github.thundax.bacon.inventory.infra.facade.remote.impl;

import com.github.thundax.bacon.inventory.api.dto.InventoryReservationItemDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationResultDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReserveCommandDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReleaseCommandDTO;
import com.github.thundax.bacon.inventory.api.facade.InventoryCommandFacade;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
public class InventoryCommandFacadeRemoteImpl implements InventoryCommandFacade {

    private final RestClient restClient;

    public InventoryCommandFacadeRemoteImpl(@Qualifier("inventoryRemoteRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    @Retry(name = "inventoryRemote", fallbackMethod = "reserveStockFallback")
    @CircuitBreaker(name = "inventoryRemote", fallbackMethod = "reserveStockFallback")
    @Bulkhead(name = "inventoryRemote", type = Bulkhead.Type.SEMAPHORE, fallbackMethod = "reserveStockFallback")
    public InventoryReservationResultDTO reserveStock(Long tenantId, String orderNo, List<InventoryReservationItemDTO> items) {
        return restClient.post()
                .uri("/providers/inventory/reservations/{orderNo}/reserve?tenantId={tenantId}", orderNo, tenantId)
                .body(new InventoryReserveCommandDTO(items))
                .retrieve()
                .body(InventoryReservationResultDTO.class);
    }

    @Override
    @Retry(name = "inventoryRemote", fallbackMethod = "releaseReservedStockFallback")
    @CircuitBreaker(name = "inventoryRemote", fallbackMethod = "releaseReservedStockFallback")
    @Bulkhead(name = "inventoryRemote", type = Bulkhead.Type.SEMAPHORE, fallbackMethod = "releaseReservedStockFallback")
    public InventoryReservationResultDTO releaseReservedStock(Long tenantId, String orderNo, String reason) {
        return restClient.post()
                .uri("/providers/inventory/reservations/{orderNo}/release?tenantId={tenantId}", orderNo, tenantId)
                .body(new InventoryReleaseCommandDTO(reason))
                .retrieve()
                .body(InventoryReservationResultDTO.class);
    }

    @Override
    @Retry(name = "inventoryRemote", fallbackMethod = "deductReservedStockFallback")
    @CircuitBreaker(name = "inventoryRemote", fallbackMethod = "deductReservedStockFallback")
    @Bulkhead(name = "inventoryRemote", type = Bulkhead.Type.SEMAPHORE, fallbackMethod = "deductReservedStockFallback")
    public InventoryReservationResultDTO deductReservedStock(Long tenantId, String orderNo) {
        return restClient.post()
                .uri("/providers/inventory/reservations/{orderNo}/deduct?tenantId={tenantId}", orderNo, tenantId)
                .retrieve()
                .body(InventoryReservationResultDTO.class);
    }

    @SuppressWarnings("unused")
    private InventoryReservationResultDTO reserveStockFallback(Long tenantId,
                                                               String orderNo,
                                                               List<InventoryReservationItemDTO> items,
                                                               Throwable throwable) {
        throw InventoryRemoteExceptionTranslator.translate("reserveStock", throwable);
    }

    @SuppressWarnings("unused")
    private InventoryReservationResultDTO releaseReservedStockFallback(Long tenantId,
                                                                       String orderNo,
                                                                       String reason,
                                                                       Throwable throwable) {
        throw InventoryRemoteExceptionTranslator.translate("releaseReservedStock", throwable);
    }

    @SuppressWarnings("unused")
    private InventoryReservationResultDTO deductReservedStockFallback(Long tenantId,
                                                                      String orderNo,
                                                                      Throwable throwable) {
        throw InventoryRemoteExceptionTranslator.translate("deductReservedStock", throwable);
    }
}
