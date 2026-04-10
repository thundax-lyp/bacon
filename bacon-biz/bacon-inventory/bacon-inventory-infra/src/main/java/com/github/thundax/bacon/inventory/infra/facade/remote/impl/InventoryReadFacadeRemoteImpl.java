package com.github.thundax.bacon.inventory.infra.facade.remote.impl;

import com.github.thundax.bacon.inventory.api.dto.InventoryReservationDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryStockDTO;
import com.github.thundax.bacon.inventory.api.facade.InventoryReadFacade;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
public class InventoryReadFacadeRemoteImpl implements InventoryReadFacade {

    private static final ParameterizedTypeReference<List<InventoryStockDTO>> LIST_TYPE =
            new ParameterizedTypeReference<>() {};

    private final RestClient restClient;

    public InventoryReadFacadeRemoteImpl(@Qualifier("inventoryRemoteRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    @Retry(name = "inventoryRemote", fallbackMethod = "getAvailableStockFallback")
    @CircuitBreaker(name = "inventoryRemote", fallbackMethod = "getAvailableStockFallback")
    @Bulkhead(name = "inventoryRemote", type = Bulkhead.Type.SEMAPHORE, fallbackMethod = "getAvailableStockFallback")
    public InventoryStockDTO getAvailableStock(Long skuId) {
        // 查询链路沿用和命令链路一致的 resilience/fallback 规则，避免读接口出现另一套异常语义。
        return restClient
                .get()
                .uri("/providers/inventory/stocks/{skuId}", skuId)
                .retrieve()
                .body(InventoryStockDTO.class);
    }

    @Override
    @Retry(name = "inventoryRemote", fallbackMethod = "batchGetAvailableStockFallback")
    @CircuitBreaker(name = "inventoryRemote", fallbackMethod = "batchGetAvailableStockFallback")
    @Bulkhead(
            name = "inventoryRemote",
            type = Bulkhead.Type.SEMAPHORE,
            fallbackMethod = "batchGetAvailableStockFallback")
    public List<InventoryStockDTO> batchGetAvailableStock(Set<Long> skuIds) {
        return restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/providers/inventory/stocks")
                        .queryParam("skuIds", skuIds.toArray())
                        .build())
                .retrieve()
                .body(LIST_TYPE);
    }

    @Override
    @Retry(name = "inventoryRemote", fallbackMethod = "getReservationByOrderNoFallback")
    @CircuitBreaker(name = "inventoryRemote", fallbackMethod = "getReservationByOrderNoFallback")
    @Bulkhead(
            name = "inventoryRemote",
            type = Bulkhead.Type.SEMAPHORE,
            fallbackMethod = "getReservationByOrderNoFallback")
    public InventoryReservationDTO getReservationByOrderNo(Long tenantId, String orderNo) {
        return com.github.thundax.bacon.common.core.context.BaconContextHolder.callWithTenantId(
                tenantId,
                () -> restClient
                        .get()
                        .uri("/providers/inventory/reservations/{orderNo}", orderNo)
                        .retrieve()
                        .body(InventoryReservationDTO.class));
    }

    @SuppressWarnings("unused")
    private InventoryStockDTO getAvailableStockFallback(Long skuId, Throwable throwable) {
        throw InventoryRemoteExceptionTranslator.translate("getAvailableStock", throwable);
    }

    @SuppressWarnings("unused")
    private List<InventoryStockDTO> batchGetAvailableStockFallback(Set<Long> skuIds, Throwable throwable) {
        throw InventoryRemoteExceptionTranslator.translate("batchGetAvailableStock", throwable);
    }

    @SuppressWarnings("unused")
    private InventoryReservationDTO getReservationByOrderNoFallback(
            Long tenantId, String orderNo, Throwable throwable) {
        throw InventoryRemoteExceptionTranslator.translate("getReservationByOrderNo", throwable);
    }
}
