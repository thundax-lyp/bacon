package com.github.thundax.bacon.inventory.infra.facade.remote.impl;

import com.github.thundax.bacon.inventory.api.facade.InventoryReadFacade;
import com.github.thundax.bacon.inventory.api.request.InventoryAvailableStockFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryBatchAvailableStockFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryReservationGetFacadeRequest;
import com.github.thundax.bacon.inventory.api.response.InventoryReservationFacadeResponse;
import com.github.thundax.bacon.inventory.api.response.InventoryStockFacadeResponse;
import com.github.thundax.bacon.inventory.api.response.InventoryStockListFacadeResponse;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
public class InventoryReadFacadeRemoteImpl implements InventoryReadFacade {

    private final RestClient restClient;

    public InventoryReadFacadeRemoteImpl(@Qualifier("inventoryRemoteRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    @Retry(name = "inventoryRemote", fallbackMethod = "getAvailableStockFallback")
    @CircuitBreaker(name = "inventoryRemote", fallbackMethod = "getAvailableStockFallback")
    @Bulkhead(name = "inventoryRemote", type = Bulkhead.Type.SEMAPHORE, fallbackMethod = "getAvailableStockFallback")
    public InventoryStockFacadeResponse getAvailableStock(InventoryAvailableStockFacadeRequest request) {
        // 查询链路沿用和命令链路一致的 resilience/fallback 规则，避免读接口出现另一套异常语义。
        return restClient
                .get()
                .uri("/providers/inventory/queries/available-stock?skuId={skuId}", request.getSkuId())
                .retrieve()
                .body(InventoryStockFacadeResponse.class);
    }

    @Override
    @Retry(name = "inventoryRemote", fallbackMethod = "batchGetAvailableStockFallback")
    @CircuitBreaker(name = "inventoryRemote", fallbackMethod = "batchGetAvailableStockFallback")
    @Bulkhead(
            name = "inventoryRemote",
            type = Bulkhead.Type.SEMAPHORE,
            fallbackMethod = "batchGetAvailableStockFallback")
    public InventoryStockListFacadeResponse batchGetAvailableStock(InventoryBatchAvailableStockFacadeRequest request) {
        InventoryStockFacadeResponse[] records = restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/providers/inventory/queries/available-stocks")
                        .queryParam(
                                "skuIds",
                                request.getSkuIds() == null ? new Object[0] : request.getSkuIds().toArray())
                        .build())
                .retrieve()
                .body(InventoryStockFacadeResponse[].class);
        return new InventoryStockListFacadeResponse(records == null ? List.of() : Arrays.asList(records));
    }

    @Override
    @Retry(name = "inventoryRemote", fallbackMethod = "getReservationByOrderNoFallback")
    @CircuitBreaker(name = "inventoryRemote", fallbackMethod = "getReservationByOrderNoFallback")
    @Bulkhead(
            name = "inventoryRemote",
            type = Bulkhead.Type.SEMAPHORE,
            fallbackMethod = "getReservationByOrderNoFallback")
    public InventoryReservationFacadeResponse getReservationByOrderNo(InventoryReservationGetFacadeRequest request) {
        return restClient
                .get()
                .uri("/providers/inventory/queries/reservation?orderNo={orderNo}", request.getOrderNo())
                .retrieve()
                .body(InventoryReservationFacadeResponse.class);
    }

    @SuppressWarnings("unused")
    private InventoryStockFacadeResponse getAvailableStockFallback(
            InventoryAvailableStockFacadeRequest request, Throwable throwable) {
        throw InventoryRemoteExceptionTranslator.translate("getAvailableStock", throwable);
    }

    @SuppressWarnings("unused")
    private InventoryStockListFacadeResponse batchGetAvailableStockFallback(
            InventoryBatchAvailableStockFacadeRequest request, Throwable throwable) {
        throw InventoryRemoteExceptionTranslator.translate("batchGetAvailableStock", throwable);
    }

    @SuppressWarnings("unused")
    private InventoryReservationFacadeResponse getReservationByOrderNoFallback(
            InventoryReservationGetFacadeRequest request, Throwable throwable) {
        throw InventoryRemoteExceptionTranslator.translate("getReservationByOrderNo", throwable);
    }
}
