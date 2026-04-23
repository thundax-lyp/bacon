package com.github.thundax.bacon.inventory.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.thundax.bacon.inventory.api.request.InventoryAvailableStockFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryBatchAvailableStockFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryDeductFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryReleaseFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryReservationGetFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryReservationItemFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryReserveFacadeRequest;
import com.github.thundax.bacon.inventory.api.response.InventoryReservationFacadeResponse;
import com.github.thundax.bacon.inventory.api.response.InventoryReservationItemFacadeResponse;
import com.github.thundax.bacon.inventory.api.response.InventoryStockFacadeResponse;
import com.github.thundax.bacon.inventory.api.response.InventoryStockListFacadeResponse;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class InventoryFacadeContractTest {

    @Test
    void shouldKeepReadRequestContracts() throws Exception {
        assertField(InventoryAvailableStockFacadeRequest.class, "skuId", Long.class);
        assertField(InventoryBatchAvailableStockFacadeRequest.class, "skuIds", Set.class);
        assertField(InventoryReservationGetFacadeRequest.class, "orderNo", String.class);

        assertThat(new InventoryAvailableStockFacadeRequest(101L).getSkuId()).isEqualTo(101L);
        assertThat(new InventoryBatchAvailableStockFacadeRequest(new LinkedHashSet<>(List.of(101L, 102L))).getSkuIds())
                .containsExactly(101L, 102L);
        assertThat(new InventoryReservationGetFacadeRequest("ORD-1").getOrderNo()).isEqualTo("ORD-1");
    }

    @Test
    void shouldKeepCommandRequestContracts() throws Exception {
        assertField(InventoryReserveFacadeRequest.class, "orderNo", String.class);
        assertField(InventoryReserveFacadeRequest.class, "items", List.class);
        assertField(InventoryReservationItemFacadeRequest.class, "skuId", Long.class);
        assertField(InventoryReservationItemFacadeRequest.class, "quantity", Integer.class);
        assertField(InventoryReleaseFacadeRequest.class, "orderNo", String.class);
        assertField(InventoryReleaseFacadeRequest.class, "reason", String.class);
        assertField(InventoryDeductFacadeRequest.class, "orderNo", String.class);

        InventoryReserveFacadeRequest reserve = new InventoryReserveFacadeRequest(
                "ORD-1", List.of(new InventoryReservationItemFacadeRequest(101L, 2)));

        assertThat(reserve.getOrderNo()).isEqualTo("ORD-1");
        assertThat(reserve.getItems().get(0).getSkuId()).isEqualTo(101L);
        assertThat(new InventoryReleaseFacadeRequest("ORD-1", "TIMEOUT_CLOSED").getReason())
                .isEqualTo("TIMEOUT_CLOSED");
        assertThat(new InventoryDeductFacadeRequest("ORD-1").getOrderNo()).isEqualTo("ORD-1");
    }

    @Test
    void shouldKeepStockResponseContracts() throws Exception {
        assertField(InventoryStockFacadeResponse.class, "skuId", Long.class);
        assertField(InventoryStockFacadeResponse.class, "warehouseCode", String.class);
        assertField(InventoryStockFacadeResponse.class, "onHandQuantity", Integer.class);
        assertField(InventoryStockFacadeResponse.class, "reservedQuantity", Integer.class);
        assertField(InventoryStockFacadeResponse.class, "availableQuantity", Integer.class);
        assertField(InventoryStockFacadeResponse.class, "status", String.class);
        assertField(InventoryStockFacadeResponse.class, "updatedAt", Instant.class);
        assertField(InventoryStockListFacadeResponse.class, "records", List.class);

        InventoryStockFacadeResponse stock = new InventoryStockFacadeResponse(
                101L, "DEFAULT", 100, 20, 80, "ENABLED", Instant.parse("2026-03-26T10:00:00Z"));
        InventoryStockListFacadeResponse list = new InventoryStockListFacadeResponse(List.of(stock));

        assertThat(stock.getAvailableQuantity()).isEqualTo(80);
        assertThat(list.getRecords()).extracting(InventoryStockFacadeResponse::getSkuId).containsExactly(101L);
    }

    @Test
    void shouldKeepReservationResponseContracts() throws Exception {
        assertField(InventoryReservationFacadeResponse.class, "orderNo", String.class);
        assertField(InventoryReservationFacadeResponse.class, "reservationNo", String.class);
        assertField(InventoryReservationFacadeResponse.class, "reservationStatus", String.class);
        assertField(InventoryReservationFacadeResponse.class, "inventoryStatus", String.class);
        assertField(InventoryReservationFacadeResponse.class, "warehouseCode", String.class);
        assertField(InventoryReservationFacadeResponse.class, "items", List.class);
        assertField(InventoryReservationFacadeResponse.class, "failureReason", String.class);
        assertField(InventoryReservationFacadeResponse.class, "releaseReason", String.class);
        assertField(InventoryReservationFacadeResponse.class, "createdAt", Instant.class);
        assertField(InventoryReservationFacadeResponse.class, "releasedAt", Instant.class);
        assertField(InventoryReservationFacadeResponse.class, "deductedAt", Instant.class);
        assertField(InventoryReservationItemFacadeResponse.class, "skuId", Long.class);
        assertField(InventoryReservationItemFacadeResponse.class, "quantity", Integer.class);

        InventoryReservationFacadeResponse response = new InventoryReservationFacadeResponse(
                "ORD-1",
                "RSV-1",
                "RESERVED",
                "RESERVED",
                "DEFAULT",
                List.of(new InventoryReservationItemFacadeResponse(101L, 2)),
                null,
                null,
                Instant.parse("2026-03-26T10:00:00Z"),
                null,
                null);

        assertThat(response.getReservationNo()).isEqualTo("RSV-1");
        assertThat(response.getInventoryStatus()).isEqualTo("RESERVED");
        assertThat(response.getItems().get(0).getQuantity()).isEqualTo(2);
    }

    private void assertField(Class<?> type, String fieldName, Class<?> fieldType) throws Exception {
        Field field = type.getDeclaredField(fieldName);
        assertThat(field.getType()).isEqualTo(fieldType);
    }
}
