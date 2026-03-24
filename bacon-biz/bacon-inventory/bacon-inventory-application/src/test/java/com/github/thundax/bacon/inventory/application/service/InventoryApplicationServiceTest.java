package com.github.thundax.bacon.inventory.application.service;

import com.github.thundax.bacon.inventory.api.dto.InventoryReservationItemDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationResultDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryStockDTO;
import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.entity.InventoryLedger;
import com.github.thundax.bacon.inventory.domain.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.repository.InventoryRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InventoryApplicationServiceTest {

    @Test
    void reserveStockShouldBeIdempotentAndUpdateAvailableQuantity() {
        TestInventoryRepository repository = new TestInventoryRepository();
        InventoryOperationLogService operationLogService = new InventoryOperationLogService(repository);
        InventoryApplicationService service = new InventoryApplicationService(
                new InventoryReservationApplicationService(repository, operationLogService),
                new InventoryReleaseApplicationService(repository, operationLogService),
                new InventoryDeductionApplicationService(repository, operationLogService)
        );
        InventoryQueryService queryService = new InventoryQueryService(repository);

        InventoryReservationResultDTO first = service.reserveStock(1001L, "ORDER-1",
                List.of(new InventoryReservationItemDTO(101L, 10)));
        InventoryReservationResultDTO second = service.reserveStock(1001L, "ORDER-1",
                List.of(new InventoryReservationItemDTO(101L, 10)));
        InventoryStockDTO stock = queryService.getAvailableStock(1001L, 101L);

        assertEquals(InventoryReservation.STATUS_RESERVED, first.getReservationStatus());
        assertEquals("RESERVED", first.getInventoryStatus());
        assertEquals(first.getReservationNo(), second.getReservationNo());
        assertEquals(10, stock.getReservedQuantity());
        assertEquals(90, stock.getAvailableQuantity());
        assertEquals(1, queryService.listLedgersByOrderNo(1001L, "ORDER-1").size());
        assertEquals(InventoryLedger.TYPE_RESERVE,
                queryService.listLedgersByOrderNo(1001L, "ORDER-1").get(0).getLedgerType());
    }

    @Test
    void reserveStockShouldReturnFailedWhenStockIsInsufficient() {
        TestInventoryRepository repository = new TestInventoryRepository();
        InventoryOperationLogService operationLogService = new InventoryOperationLogService(repository);
        InventoryApplicationService service = new InventoryApplicationService(
                new InventoryReservationApplicationService(repository, operationLogService),
                new InventoryReleaseApplicationService(repository, operationLogService),
                new InventoryDeductionApplicationService(repository, operationLogService)
        );
        InventoryQueryService queryService = new InventoryQueryService(repository);

        InventoryReservationResultDTO result = service.reserveStock(1001L, "ORDER-2",
                List.of(new InventoryReservationItemDTO(101L, 1000)));
        InventoryStockDTO stock = queryService.getAvailableStock(1001L, 101L);

        assertEquals(InventoryReservation.STATUS_FAILED, result.getReservationStatus());
        assertEquals("FAILED", result.getInventoryStatus());
        assertNotNull(result.getFailureReason());
        assertEquals(0, stock.getReservedQuantity());
        assertEquals(100, stock.getAvailableQuantity());
        assertEquals(InventoryAuditLog.ACTION_RESERVE_FAILED,
                queryService.listAuditLogsByOrderNo(1001L, "ORDER-2").get(0).getActionType());
    }

    @Test
    void releaseReservedStockShouldBeIdempotent() {
        TestInventoryRepository repository = new TestInventoryRepository();
        InventoryOperationLogService operationLogService = new InventoryOperationLogService(repository);
        InventoryApplicationService service = new InventoryApplicationService(
                new InventoryReservationApplicationService(repository, operationLogService),
                new InventoryReleaseApplicationService(repository, operationLogService),
                new InventoryDeductionApplicationService(repository, operationLogService)
        );
        InventoryQueryService queryService = new InventoryQueryService(repository);

        service.reserveStock(1001L, "ORDER-3", List.of(new InventoryReservationItemDTO(101L, 5)));
        InventoryReservationResultDTO firstRelease = service.releaseReservedStock(1001L, "ORDER-3", "USER_CANCELLED");
        InventoryReservationResultDTO secondRelease = service.releaseReservedStock(1001L, "ORDER-3", "USER_CANCELLED");
        InventoryStockDTO stock = queryService.getAvailableStock(1001L, 101L);

        assertEquals(InventoryReservation.STATUS_RELEASED, firstRelease.getReservationStatus());
        assertEquals(InventoryReservation.STATUS_RELEASED, secondRelease.getReservationStatus());
        assertEquals(0, stock.getReservedQuantity());
        assertEquals(100, stock.getAvailableQuantity());
        assertEquals(2, queryService.listLedgersByOrderNo(1001L, "ORDER-3").size());
        assertEquals(InventoryAuditLog.ACTION_RELEASE,
                queryService.listAuditLogsByOrderNo(1001L, "ORDER-3").get(1).getActionType());
    }

    @Test
    void deductReservedStockShouldBeIdempotent() {
        TestInventoryRepository repository = new TestInventoryRepository();
        InventoryOperationLogService operationLogService = new InventoryOperationLogService(repository);
        InventoryApplicationService service = new InventoryApplicationService(
                new InventoryReservationApplicationService(repository, operationLogService),
                new InventoryReleaseApplicationService(repository, operationLogService),
                new InventoryDeductionApplicationService(repository, operationLogService)
        );
        InventoryQueryService queryService = new InventoryQueryService(repository);

        service.reserveStock(1001L, "ORDER-4", List.of(new InventoryReservationItemDTO(101L, 7)));
        InventoryReservationResultDTO firstDeduct = service.deductReservedStock(1001L, "ORDER-4");
        InventoryReservationResultDTO secondDeduct = service.deductReservedStock(1001L, "ORDER-4");
        InventoryStockDTO stock = queryService.getAvailableStock(1001L, 101L);

        assertEquals(InventoryReservation.STATUS_DEDUCTED, firstDeduct.getReservationStatus());
        assertEquals(InventoryReservation.STATUS_DEDUCTED, secondDeduct.getReservationStatus());
        assertEquals(0, stock.getReservedQuantity());
        assertEquals(93, stock.getOnHandQuantity());
        assertEquals(93, stock.getAvailableQuantity());
        assertEquals(2, queryService.listLedgersByOrderNo(1001L, "ORDER-4").size());
        assertEquals(InventoryLedger.TYPE_DEDUCT,
                queryService.listLedgersByOrderNo(1001L, "ORDER-4").get(1).getLedgerType());
    }

    private static final class TestInventoryRepository implements InventoryRepository {

        private final Map<String, Inventory> inventories = new ConcurrentHashMap<>();
        private final Map<String, InventoryReservation> reservations = new ConcurrentHashMap<>();
        private final Map<String, List<InventoryLedger>> ledgers = new ConcurrentHashMap<>();
        private final Map<String, List<InventoryAuditLog>> auditLogs = new ConcurrentHashMap<>();

        private TestInventoryRepository() {
            inventories.put(key(1001L, 101L), new Inventory(1L, 1001L, 101L, 1L, 100, 0, 100,
                    Inventory.STATUS_ENABLED, Instant.now()));
        }

        @Override
        public Optional<Inventory> findInventory(Long tenantId, Long skuId) {
            return Optional.ofNullable(inventories.get(key(tenantId, skuId)));
        }

        @Override
        public List<Inventory> findInventories(Long tenantId, Set<Long> skuIds) {
            return skuIds.stream()
                    .map(skuId -> inventories.get(key(tenantId, skuId)))
                    .filter(java.util.Objects::nonNull)
                    .toList();
        }

        @Override
        public InventoryReservation saveReservation(InventoryReservation reservation) {
            reservations.put(reservationKey(reservation.getTenantId(), reservation.getOrderNo()), reservation);
            return reservation;
        }

        @Override
        public Optional<InventoryReservation> findReservation(Long tenantId, String orderNo) {
            return Optional.ofNullable(reservations.get(reservationKey(tenantId, orderNo)));
        }

        @Override
        public void saveLedger(InventoryLedger ledger) {
            ledgers.computeIfAbsent(reservationKey(ledger.getTenantId(), ledger.getOrderNo()), ignored -> new java.util.ArrayList<>())
                    .add(ledger);
        }

        @Override
        public List<InventoryLedger> findLedgers(Long tenantId, String orderNo) {
            return List.copyOf(ledgers.getOrDefault(reservationKey(tenantId, orderNo), List.of()));
        }

        @Override
        public void saveAuditLog(InventoryAuditLog auditLog) {
            auditLogs.computeIfAbsent(reservationKey(auditLog.getTenantId(), auditLog.getOrderNo()),
                    ignored -> new java.util.ArrayList<>()).add(auditLog);
        }

        @Override
        public List<InventoryAuditLog> findAuditLogs(Long tenantId, String orderNo) {
            return List.copyOf(auditLogs.getOrDefault(reservationKey(tenantId, orderNo), List.of()));
        }

        private static String key(Long tenantId, Long skuId) {
            return tenantId + ":" + skuId;
        }

        private static String reservationKey(Long tenantId, String orderNo) {
            return tenantId + ":" + orderNo;
        }
    }
}
