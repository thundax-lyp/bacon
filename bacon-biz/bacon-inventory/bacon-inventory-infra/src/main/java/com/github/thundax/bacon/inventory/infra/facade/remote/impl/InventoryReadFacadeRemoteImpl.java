package com.github.thundax.bacon.inventory.infra.facade.remote.impl;

import com.github.thundax.bacon.inventory.api.dto.InventoryReservationDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryStockDTO;
import com.github.thundax.bacon.inventory.api.facade.InventoryReadFacade;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Qualifier;
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

    public InventoryReadFacadeRemoteImpl(@Qualifier("inventoryRemoteRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    @Retry(name = "inventoryRemote", fallbackMethod = "getAvailableStockFallback")
    @CircuitBreaker(name = "inventoryRemote", fallbackMethod = "getAvailableStockFallback")
    @Bulkhead(name = "inventoryRemote", type = Bulkhead.Type.SEMAPHORE, fallbackMethod = "getAvailableStockFallback")
    public InventoryStockDTO getAvailableStock(Long tenantId, Long skuId) {
        return restClient.get()
                .uri("/providers/inventory/stocks/{skuId}?tenantId={tenantId}", skuId, tenantId)
                .retrieve()
                .body(InventoryStockDTO.class);
    }

    @Override
    @Retry(name = "inventoryRemote", fallbackMethod = "batchGetAvailableStockFallback")
    @CircuitBreaker(name = "inventoryRemote", fallbackMethod = "batchGetAvailableStockFallback")
    @Bulkhead(name = "inventoryRemote", type = Bulkhead.Type.SEMAPHORE, fallbackMethod = "batchGetAvailableStockFallback")
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
    @Retry(name = "inventoryRemote", fallbackMethod = "getReservationByOrderNoFallback")
    @CircuitBreaker(name = "inventoryRemote", fallbackMethod = "getReservationByOrderNoFallback")
    @Bulkhead(name = "inventoryRemote", type = Bulkhead.Type.SEMAPHORE, fallbackMethod = "getReservationByOrderNoFallback")
    public InventoryReservationDTO getReservationByOrderNo(Long tenantId, String orderNo) {
        return restClient.get()
                .uri("/providers/inventory/reservations/{orderNo}?tenantId={tenantId}", orderNo, tenantId)
                .retrieve()
                .body(InventoryReservationDTO.class);
    }

    @SuppressWarnings("unused")
    private InventoryStockDTO getAvailableStockFallback(Long tenantId, Long skuId, Throwable throwable) {
        throw InventoryRemoteExceptionTranslator.translate("getAvailableStock", throwable);
    }

    @SuppressWarnings("unused")
    private List<InventoryStockDTO> batchGetAvailableStockFallback(Long tenantId,
                                                                   Set<Long> skuIds,
                                                                   Throwable throwable) {
        throw InventoryRemoteExceptionTranslator.translate("batchGetAvailableStock", throwable);
    }

    @SuppressWarnings("unused")
    private InventoryReservationDTO getReservationByOrderNoFallback(Long tenantId,
                                                                    String orderNo,
                                                                    Throwable throwable) {
        throw InventoryRemoteExceptionTranslator.translate("getReservationByOrderNo", throwable);
    }
}
