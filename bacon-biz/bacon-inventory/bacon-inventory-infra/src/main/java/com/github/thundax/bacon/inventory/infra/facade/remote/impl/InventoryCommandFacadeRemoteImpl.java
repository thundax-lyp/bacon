package com.github.thundax.bacon.inventory.infra.facade.remote.impl;

import com.github.thundax.bacon.inventory.api.facade.InventoryCommandFacade;
import com.github.thundax.bacon.inventory.api.request.InventoryDeductFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryReleaseFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryReserveFacadeRequest;
import com.github.thundax.bacon.inventory.api.response.InventoryReservationFacadeResponse;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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
    public InventoryReservationFacadeResponse reserveStock(InventoryReserveFacadeRequest request) {
        // reserve/release/deduct 都只转发 provider 契约，不在 remote facade 里实现任何库存业务降级。
        return restClient
                .post()
                .uri("/providers/inventory/reservations/reserve")
                .body(request)
                .retrieve()
                .body(InventoryReservationFacadeResponse.class);
    }

    @Override
    @Retry(name = "inventoryRemote", fallbackMethod = "releaseReservedStockFallback")
    @CircuitBreaker(name = "inventoryRemote", fallbackMethod = "releaseReservedStockFallback")
    @Bulkhead(name = "inventoryRemote", type = Bulkhead.Type.SEMAPHORE, fallbackMethod = "releaseReservedStockFallback")
    public InventoryReservationFacadeResponse releaseReservedStock(InventoryReleaseFacadeRequest request) {
        return restClient
                .post()
                .uri("/providers/inventory/reservations/release")
                .body(request)
                .retrieve()
                .body(InventoryReservationFacadeResponse.class);
    }

    @Override
    @Retry(name = "inventoryRemote", fallbackMethod = "deductReservedStockFallback")
    @CircuitBreaker(name = "inventoryRemote", fallbackMethod = "deductReservedStockFallback")
    @Bulkhead(name = "inventoryRemote", type = Bulkhead.Type.SEMAPHORE, fallbackMethod = "deductReservedStockFallback")
    public InventoryReservationFacadeResponse deductReservedStock(InventoryDeductFacadeRequest request) {
        return restClient
                .post()
                .uri("/providers/inventory/reservations/deduct")
                .body(request)
                .retrieve()
                .body(InventoryReservationFacadeResponse.class);
    }

    @SuppressWarnings("unused")
    private InventoryReservationFacadeResponse reserveStockFallback(
            InventoryReserveFacadeRequest request, Throwable throwable) {
        // fallback 统一收敛为库存领域异常，调用方据此决定走补偿、重试还是主流程失败。
        throw InventoryRemoteExceptionTranslator.translate("reserveStock", throwable);
    }

    @SuppressWarnings("unused")
    private InventoryReservationFacadeResponse releaseReservedStockFallback(
            InventoryReleaseFacadeRequest request, Throwable throwable) {
        throw InventoryRemoteExceptionTranslator.translate("releaseReservedStock", throwable);
    }

    @SuppressWarnings("unused")
    private InventoryReservationFacadeResponse deductReservedStockFallback(
            InventoryDeductFacadeRequest request, Throwable throwable) {
        throw InventoryRemoteExceptionTranslator.translate("deductReservedStock", throwable);
    }
}
