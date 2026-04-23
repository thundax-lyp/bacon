package com.github.thundax.bacon.inventory.interfaces.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.context.BaconContextHolder.BaconContext;
import com.github.thundax.bacon.inventory.api.request.InventoryAvailableStockFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryBatchAvailableStockFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryDeductFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryReleaseFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryReservationGetFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryReservationItemFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryReserveFacadeRequest;
import com.github.thundax.bacon.inventory.api.response.InventoryReservationFacadeResponse;
import com.github.thundax.bacon.inventory.api.response.InventoryStockFacadeResponse;
import com.github.thundax.bacon.inventory.api.response.InventoryStockListFacadeResponse;
import com.github.thundax.bacon.inventory.application.command.InventoryCommandApplicationService;
import com.github.thundax.bacon.inventory.application.dto.InventoryReservationDTO;
import com.github.thundax.bacon.inventory.application.dto.InventoryReservationItemDTO;
import com.github.thundax.bacon.inventory.application.dto.InventoryStockDTO;
import com.github.thundax.bacon.inventory.application.query.InventoryQueryApplicationService;
import com.github.thundax.bacon.inventory.application.result.InventoryReservationResult;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InventoryFacadeLocalContractTest {

    @Mock
    private InventoryQueryApplicationService inventoryQueryApplicationService;

    @Mock
    private InventoryCommandApplicationService inventoryCommandApplicationService;

    @BeforeEach
    void setUp() {
        BaconContextHolder.set(new BaconContext(1001L, 2001L));
    }

    @AfterEach
    void tearDown() {
        BaconContextHolder.clear();
    }

    @Test
    void shouldMapAvailableStockFacadeToApplicationQueries() {
        when(inventoryQueryApplicationService.getAvailableStock(
                        argThat(query -> query != null && query.skuId().value().equals(101L))))
                .thenReturn(stockDto(101L));
        when(inventoryQueryApplicationService.batchGetAvailableStock(argThat(query -> query != null
                        && query.skuIds().stream().map(skuId -> skuId.value()).collect(java.util.stream.Collectors.toSet())
                                .equals(Set.of(101L, 102L)))))
                .thenReturn(List.of(stockDto(101L), stockDto(102L)));
        InventoryReadFacadeLocalImpl facade = new InventoryReadFacadeLocalImpl(inventoryQueryApplicationService);

        InventoryStockFacadeResponse stock = facade.getAvailableStock(new InventoryAvailableStockFacadeRequest(101L));
        InventoryStockListFacadeResponse stocks = facade.batchGetAvailableStock(
                new InventoryBatchAvailableStockFacadeRequest(new LinkedHashSet<>(List.of(101L, 102L))));

        assertThat(stock.getSkuId()).isEqualTo(101L);
        assertThat(stock.getAvailableQuantity()).isEqualTo(80);
        assertThat(stocks.getRecords()).extracting(InventoryStockFacadeResponse::getSkuId).containsExactly(101L, 102L);
    }

    @Test
    void shouldMapReservationReadFacadeToApplicationQuery() {
        when(inventoryQueryApplicationService.getReservationByOrderNo(argThat(
                        query -> query != null && query.orderNo().value().equals("ORD-1"))))
                .thenReturn(reservationDto());
        InventoryReadFacadeLocalImpl facade = new InventoryReadFacadeLocalImpl(inventoryQueryApplicationService);

        InventoryReservationFacadeResponse response =
                facade.getReservationByOrderNo(new InventoryReservationGetFacadeRequest("ORD-1"));

        assertThat(response.getOrderNo()).isEqualTo("ORD-1");
        assertThat(response.getReservationStatus()).isEqualTo("RESERVED");
        assertThat(response.getInventoryStatus()).isEqualTo("RESERVED");
        assertThat(response.getItems()).hasSize(1);
    }

    @Test
    void shouldMapCommandFacadeToApplicationCommandsAndResults() {
        when(inventoryCommandApplicationService.reserveStock(argThat(command -> command != null
                        && command.orderNo().value().equals("ORD-1")
                        && command.items().size() == 1
                        && command.items().get(0).skuId().value().equals(101L)
                        && command.items().get(0).quantity().equals(2))))
                .thenReturn(reservedResult());
        when(inventoryCommandApplicationService.releaseReservedStock(argThat(command -> command != null
                        && command.orderNo().value().equals("ORD-1")
                        && command.reason().name().equals("TIMEOUT_CLOSED"))))
                .thenReturn(releasedResult());
        when(inventoryCommandApplicationService.deductReservedStock(argThat(
                        command -> command != null && command.orderNo().value().equals("ORD-1"))))
                .thenReturn(deductedResult());
        InventoryCommandFacadeLocalImpl facade = new InventoryCommandFacadeLocalImpl(inventoryCommandApplicationService);

        InventoryReservationFacadeResponse reserved = facade.reserveStock(new InventoryReserveFacadeRequest(
                "ORD-1", List.of(new InventoryReservationItemFacadeRequest(101L, 2))));
        InventoryReservationFacadeResponse released =
                facade.releaseReservedStock(new InventoryReleaseFacadeRequest("ORD-1", "TIMEOUT_CLOSED"));
        InventoryReservationFacadeResponse deducted =
                facade.deductReservedStock(new InventoryDeductFacadeRequest("ORD-1"));

        assertThat(reserved.getInventoryStatus()).isEqualTo("RESERVED");
        assertThat(released.getInventoryStatus()).isEqualTo("RELEASED");
        assertThat(released.getReleaseReason()).isEqualTo("TIMEOUT_CLOSED");
        assertThat(deducted.getInventoryStatus()).isEqualTo("DEDUCTED");
        verify(inventoryCommandApplicationService).reserveStock(argThat(command -> command.orderNo().value().equals("ORD-1")));
    }

    private InventoryStockDTO stockDto(Long skuId) {
        return new InventoryStockDTO(
                skuId, "DEFAULT", 100, 20, 80, "ENABLED", Instant.parse("2026-03-26T10:00:00Z"));
    }

    private InventoryReservationDTO reservationDto() {
        return new InventoryReservationDTO(
                "ORD-1",
                "RSV-1",
                "RESERVED",
                "DEFAULT",
                List.of(new InventoryReservationItemDTO(101L, 2)),
                null,
                null,
                Instant.parse("2026-03-26T10:00:00Z"),
                null,
                null);
    }

    private InventoryReservationResult reservedResult() {
        return new InventoryReservationResult("ORD-1", "RSV-1", "RESERVED", "RESERVED", "DEFAULT", null, null, null, null);
    }

    private InventoryReservationResult releasedResult() {
        return new InventoryReservationResult(
                "ORD-1",
                "RSV-1",
                "RELEASED",
                "RELEASED",
                "DEFAULT",
                null,
                "TIMEOUT_CLOSED",
                Instant.parse("2026-03-26T10:30:00Z"),
                null);
    }

    private InventoryReservationResult deductedResult() {
        return new InventoryReservationResult(
                "ORD-1",
                "RSV-1",
                "DEDUCTED",
                "DEDUCTED",
                "DEFAULT",
                null,
                null,
                null,
                Instant.parse("2026-03-26T10:40:00Z"));
    }
}
