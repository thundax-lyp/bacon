package com.github.thundax.bacon.inventory.application;

import com.github.thundax.bacon.inventory.application.audit.InventoryOperationLogSupport;
import com.github.thundax.bacon.inventory.application.command.InventoryApplicationService;
import com.github.thundax.bacon.inventory.application.command.InventoryDeductionApplicationService;
import com.github.thundax.bacon.inventory.application.command.InventoryReleaseApplicationService;
import com.github.thundax.bacon.inventory.application.command.InventoryReservationApplicationService;
import com.github.thundax.bacon.inventory.application.query.InventoryQueryApplicationService;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationItemDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationResultDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryStockDTO;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryLedger;
import com.github.thundax.bacon.inventory.domain.model.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditActionType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryLedgerType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.inventory.domain.repository.InventoryAuditDeadLetterRepository;
import com.github.thundax.bacon.inventory.domain.repository.InventoryAuditOutboxRepository;
import com.github.thundax.bacon.inventory.domain.repository.InventoryAuditRecordRepository;
import com.github.thundax.bacon.inventory.domain.repository.InventoryReservationRepository;
import com.github.thundax.bacon.inventory.domain.repository.InventoryStockRepository;
import com.github.thundax.bacon.inventory.domain.service.InventoryReservationNoGenerator;
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

    private static final InventoryReservationNoGenerator RESERVATION_NO_GENERATOR =
            new SequenceInventoryReservationNoGenerator();

    @Test
    void reserveStockShouldBeIdempotentAndUpdateAvailableQuantity() {
        TestInventoryRepository repository = new TestInventoryRepository();
        InventoryOperationLogSupport operationLogService = new InventoryOperationLogSupport(repository, repository);
        InventoryApplicationService service = new InventoryApplicationService(
                new InventoryReservationApplicationService(repository, repository, operationLogService,
                        RESERVATION_NO_GENERATOR),
                new InventoryReleaseApplicationService(repository, repository, operationLogService),
                new InventoryDeductionApplicationService(repository, repository, operationLogService)
        );
        InventoryQueryApplicationService queryService = new InventoryQueryApplicationService(repository, repository, repository, repository);

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
        assertEquals(InventoryLedgerType.RESERVE.value(),
                queryService.listLedgersByOrderNo(1001L, "ORDER-1").get(0).getLedgerType());
    }

    @Test
    void reserveStockShouldReturnFailedWhenStockIsInsufficient() {
        TestInventoryRepository repository = new TestInventoryRepository();
        InventoryOperationLogSupport operationLogService = new InventoryOperationLogSupport(repository, repository);
        InventoryApplicationService service = new InventoryApplicationService(
                new InventoryReservationApplicationService(repository, repository, operationLogService,
                        RESERVATION_NO_GENERATOR),
                new InventoryReleaseApplicationService(repository, repository, operationLogService),
                new InventoryDeductionApplicationService(repository, repository, operationLogService)
        );
        InventoryQueryApplicationService queryService = new InventoryQueryApplicationService(repository, repository, repository, repository);

        InventoryReservationResultDTO result = service.reserveStock(1001L, "ORDER-2",
                List.of(new InventoryReservationItemDTO(101L, 1000)));
        InventoryStockDTO stock = queryService.getAvailableStock(1001L, 101L);

        assertEquals(InventoryReservation.STATUS_FAILED, result.getReservationStatus());
        assertEquals("FAILED", result.getInventoryStatus());
        assertNotNull(result.getFailureReason());
        assertEquals(0, stock.getReservedQuantity());
        assertEquals(100, stock.getAvailableQuantity());
        assertEquals(InventoryAuditActionType.RESERVE_FAILED.value(),
                queryService.listAuditLogsByOrderNo(1001L, "ORDER-2").get(0).getActionType());
    }

    @Test
    void releaseReservedStockShouldBeIdempotent() {
        TestInventoryRepository repository = new TestInventoryRepository();
        InventoryOperationLogSupport operationLogService = new InventoryOperationLogSupport(repository, repository);
        InventoryApplicationService service = new InventoryApplicationService(
                new InventoryReservationApplicationService(repository, repository, operationLogService,
                        RESERVATION_NO_GENERATOR),
                new InventoryReleaseApplicationService(repository, repository, operationLogService),
                new InventoryDeductionApplicationService(repository, repository, operationLogService)
        );
        InventoryQueryApplicationService queryService = new InventoryQueryApplicationService(repository, repository, repository, repository);

        service.reserveStock(1001L, "ORDER-3", List.of(new InventoryReservationItemDTO(101L, 5)));
        InventoryReservationResultDTO firstRelease = service.releaseReservedStock(1001L, "ORDER-3", "USER_CANCELLED");
        InventoryReservationResultDTO secondRelease = service.releaseReservedStock(1001L, "ORDER-3", "USER_CANCELLED");
        InventoryStockDTO stock = queryService.getAvailableStock(1001L, 101L);

        assertEquals(InventoryReservation.STATUS_RELEASED, firstRelease.getReservationStatus());
        assertEquals(InventoryReservation.STATUS_RELEASED, secondRelease.getReservationStatus());
        assertEquals(0, stock.getReservedQuantity());
        assertEquals(100, stock.getAvailableQuantity());
        assertEquals(2, queryService.listLedgersByOrderNo(1001L, "ORDER-3").size());
        assertEquals(InventoryAuditActionType.RELEASE.value(),
                queryService.listAuditLogsByOrderNo(1001L, "ORDER-3").get(1).getActionType());
    }

    @Test
    void deductReservedStockShouldBeIdempotent() {
        TestInventoryRepository repository = new TestInventoryRepository();
        InventoryOperationLogSupport operationLogService = new InventoryOperationLogSupport(repository, repository);
        InventoryApplicationService service = new InventoryApplicationService(
                new InventoryReservationApplicationService(repository, repository, operationLogService,
                        RESERVATION_NO_GENERATOR),
                new InventoryReleaseApplicationService(repository, repository, operationLogService),
                new InventoryDeductionApplicationService(repository, repository, operationLogService)
        );
        InventoryQueryApplicationService queryService = new InventoryQueryApplicationService(repository, repository, repository, repository);

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
        assertEquals(InventoryLedgerType.DEDUCT.value(),
                queryService.listLedgersByOrderNo(1001L, "ORDER-4").get(1).getLedgerType());
    }

    @Test
    void reserveStockShouldBatchLoadInventoriesWithoutPerSkuPreRead() {
        TestInventoryRepository repository = new TestInventoryRepository();
        InventoryOperationLogSupport operationLogService = new InventoryOperationLogSupport(repository, repository);
        InventoryApplicationService service = new InventoryApplicationService(
                new InventoryReservationApplicationService(repository, repository, operationLogService,
                        RESERVATION_NO_GENERATOR),
                new InventoryReleaseApplicationService(repository, repository, operationLogService),
                new InventoryDeductionApplicationService(repository, repository, operationLogService)
        );

        service.reserveStock(1001L, "ORDER-5", List.of(
                new InventoryReservationItemDTO(101L, 2),
                new InventoryReservationItemDTO(101L, 3)));

        assertEquals(1, repository.getBatchFindInventoriesCallCount());
        assertEquals(0, repository.getSingleFindInventoryCallCount());
    }

    private static final class TestInventoryRepository implements InventoryStockRepository, InventoryReservationRepository,
            InventoryAuditRecordRepository, InventoryAuditOutboxRepository, InventoryAuditDeadLetterRepository {

        private final Map<String, Inventory> inventories = new ConcurrentHashMap<>();
        private final Map<String, InventoryReservation> reservations = new ConcurrentHashMap<>();
        private final Map<String, List<InventoryLedger>> ledgers = new ConcurrentHashMap<>();
        private final Map<String, List<InventoryAuditLog>> auditLogs = new ConcurrentHashMap<>();
        private int singleFindInventoryCallCount;
        private int batchFindInventoriesCallCount;

        private TestInventoryRepository() {
            inventories.put(key(1001L, 101L), new Inventory(1L, 1001L, 101L, "DEFAULT", 100, 0, 100,
                    InventoryStatus.ENABLED, 0L, Instant.now()));
        }

        @Override
        public Optional<Inventory> findInventory(Long tenantId, Long skuId) {
            singleFindInventoryCallCount++;
            return Optional.ofNullable(inventories.get(key(tenantId, skuId)));
        }

        @Override
        public List<Inventory> findInventories(Long tenantId) {
            return inventories.values().stream()
                    .filter(inventory -> inventory.getTenantId().value().equals(String.valueOf(tenantId)))
                    .toList();
        }

        @Override
        public List<Inventory> findInventories(Long tenantId, Set<Long> skuIds) {
            batchFindInventoriesCallCount++;
            return skuIds.stream()
                    .map(skuId -> inventories.get(key(tenantId, skuId)))
                    .filter(java.util.Objects::nonNull)
                    .toList();
        }

        @Override
        public List<Inventory> pageInventories(Long tenantId, Long skuId, String status, int pageNo, int pageSize) {
            return findInventories(tenantId).stream()
                    .filter(inventory -> skuId == null || inventory.getSkuId().value().equals(skuId))
                    .filter(inventory -> status == null || status.equals(inventory.getStatus().value()))
                    .skip((long) (pageNo - 1) * pageSize)
                    .limit(pageSize)
                    .toList();
        }

        @Override
        public long countInventories(Long tenantId, Long skuId, String status) {
            return findInventories(tenantId).stream()
                    .filter(inventory -> skuId == null || inventory.getSkuId().value().equals(skuId))
                    .filter(inventory -> status == null || status.equals(inventory.getStatus().value()))
                    .count();
        }

        @Override
        public Inventory saveInventory(Inventory inventory) {
            Long version = inventory.getVersion() == null ? 0L : inventory.getVersion() + 1L;
            inventory.markPersisted(version);
            inventories.put(key(inventory.getTenantId().value(), inventory.getSkuId().value()), inventory);
            return inventory;
        }

        @Override
        public InventoryReservation saveReservation(InventoryReservation reservation) {
            reservations.put(reservationKey(reservation.getTenantIdValue(), reservation.getOrderNoValue()), reservation);
            return reservation;
        }

        @Override
        public Optional<InventoryReservation> findReservation(Long tenantId, String orderNo) {
            return Optional.ofNullable(reservations.get(reservationKey(tenantId, orderNo)));
        }

        @Override
        public void saveLedger(InventoryLedger ledger) {
            ledgers.computeIfAbsent(reservationKey(ledger.getTenantIdValue(), ledger.getOrderNoValue()),
                            ignored -> new java.util.ArrayList<>())
                    .add(ledger);
        }

        @Override
        public List<InventoryLedger> findLedgers(Long tenantId, String orderNo) {
            return List.copyOf(ledgers.getOrDefault(reservationKey(tenantId, orderNo), List.of()));
        }

        @Override
        public void saveAuditLog(InventoryAuditLog auditLog) {
            auditLogs.computeIfAbsent(reservationKey(auditLog.getTenantIdValue(), auditLog.getOrderNoValue()),
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

        private int getSingleFindInventoryCallCount() {
            return singleFindInventoryCallCount;
        }

        private int getBatchFindInventoriesCallCount() {
            return batchFindInventoriesCallCount;
        }
    }

    private static final class SequenceInventoryReservationNoGenerator implements InventoryReservationNoGenerator {

        private long value = 1000L;

        @Override
        public String nextReservationNo() {
            return "RSV-" + value++;
        }
    }
}
