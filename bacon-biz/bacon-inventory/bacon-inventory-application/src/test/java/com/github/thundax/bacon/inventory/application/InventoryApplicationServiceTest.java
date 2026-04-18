package com.github.thundax.bacon.inventory.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.commerce.valueobject.WarehouseCode;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.valueobject.Version;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.inventory.application.dto.InventoryReservationItemDTO;
import com.github.thundax.bacon.inventory.application.dto.InventoryStockDTO;
import com.github.thundax.bacon.inventory.application.audit.InventoryOperationLogSupport;
import com.github.thundax.bacon.inventory.application.command.InventoryApplicationService;
import com.github.thundax.bacon.inventory.application.command.InventoryDeductionApplicationService;
import com.github.thundax.bacon.inventory.application.command.InventoryReleaseApplicationService;
import com.github.thundax.bacon.inventory.application.command.InventoryReservationApplicationService;
import com.github.thundax.bacon.inventory.application.query.InventoryQueryApplicationService;
import com.github.thundax.bacon.inventory.application.result.InventoryReservationResult;
import com.github.thundax.bacon.inventory.domain.model.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryLedger;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditActionType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryLedgerType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryReleaseReason;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryReservationStatus;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.InventoryId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.OnHandQuantity;
import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservedQuantity;
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

class InventoryApplicationServiceTest {

    private static final InventoryReservationNoGenerator RESERVATION_NO_GENERATOR =
            new SequenceInventoryReservationNoGenerator();
    private static final IdGenerator ID_GENERATOR = bizTag -> 1L;

    @Test
    void reserveStockShouldBeIdempotentAndUpdateAvailableQuantity() {
        TestInventoryRepository repository = new TestInventoryRepository();
        InventoryOperationLogSupport operationLogService =
                new InventoryOperationLogSupport(repository, repository, ID_GENERATOR);
        InventoryApplicationService service = new InventoryApplicationService(
                new InventoryReservationApplicationService(
                        repository, repository, operationLogService, RESERVATION_NO_GENERATOR, ID_GENERATOR),
                new InventoryReleaseApplicationService(repository, repository, operationLogService),
                new InventoryDeductionApplicationService(repository, repository, operationLogService));
        InventoryQueryApplicationService queryService =
                new InventoryQueryApplicationService(repository, repository, repository, repository);

        BaconContextHolder.runWithTenantId(1001L, () -> {
            InventoryReservationResult first =
                    service.reserveStock(OrderNo.of("ORDER-1"), List.of(new InventoryReservationItemDTO(101L, 10)));
            InventoryReservationResult second =
                    service.reserveStock(OrderNo.of("ORDER-1"), List.of(new InventoryReservationItemDTO(101L, 10)));
            InventoryStockDTO stock = queryService.getAvailableStock(SkuId.of(101L));
            InventoryReservation reservation =
                    repository.findByOrderNo(OrderNo.of("ORDER-1")).orElseThrow();

            assertEquals(InventoryReservationStatus.RESERVED.value(), first.getReservationStatus());
            assertEquals("RESERVED", first.getInventoryStatus());
            assertEquals(first.getReservationNo(), second.getReservationNo());
            assertNotNull(reservation.getId());
            assertNotNull(reservation.getItems().get(0).getId());
            assertEquals(10, stock.getReservedQuantity());
            assertEquals(90, stock.getAvailableQuantity());
            assertEquals(
                    1, queryService.listLedgersByOrderNo(OrderNo.of("ORDER-1")).size());
            assertEquals(
                    InventoryLedgerType.RESERVE.value(),
                    queryService
                            .listLedgersByOrderNo(OrderNo.of("ORDER-1"))
                            .get(0)
                            .getLedgerType());
        });
    }

    @Test
    void reserveStockShouldReturnFailedWhenStockIsInsufficient() {
        TestInventoryRepository repository = new TestInventoryRepository();
        InventoryOperationLogSupport operationLogService =
                new InventoryOperationLogSupport(repository, repository, ID_GENERATOR);
        InventoryApplicationService service = new InventoryApplicationService(
                new InventoryReservationApplicationService(
                        repository, repository, operationLogService, RESERVATION_NO_GENERATOR, ID_GENERATOR),
                new InventoryReleaseApplicationService(repository, repository, operationLogService),
                new InventoryDeductionApplicationService(repository, repository, operationLogService));
        InventoryQueryApplicationService queryService =
                new InventoryQueryApplicationService(repository, repository, repository, repository);

        BaconContextHolder.runWithTenantId(1001L, () -> {
            InventoryReservationResult result =
                    service.reserveStock(OrderNo.of("ORDER-2"), List.of(new InventoryReservationItemDTO(101L, 1000)));
            InventoryStockDTO stock = queryService.getAvailableStock(SkuId.of(101L));

            assertEquals(InventoryReservationStatus.FAILED.value(), result.getReservationStatus());
            assertEquals("FAILED", result.getInventoryStatus());
            assertNotNull(result.getFailureReason());
            assertEquals(0, stock.getReservedQuantity());
            assertEquals(100, stock.getAvailableQuantity());
            assertEquals(
                    InventoryAuditActionType.RESERVE_FAILED.value(),
                    queryService
                            .listAuditLogsByOrderNo(OrderNo.of("ORDER-2"))
                            .get(0)
                            .getActionType());
        });
    }

    @Test
    void releaseReservedStockShouldBeIdempotent() {
        TestInventoryRepository repository = new TestInventoryRepository();
        InventoryOperationLogSupport operationLogService =
                new InventoryOperationLogSupport(repository, repository, ID_GENERATOR);
        InventoryApplicationService service = new InventoryApplicationService(
                new InventoryReservationApplicationService(
                        repository, repository, operationLogService, RESERVATION_NO_GENERATOR, ID_GENERATOR),
                new InventoryReleaseApplicationService(repository, repository, operationLogService),
                new InventoryDeductionApplicationService(repository, repository, operationLogService));
        InventoryQueryApplicationService queryService =
                new InventoryQueryApplicationService(repository, repository, repository, repository);

        BaconContextHolder.runWithTenantId(1001L, () -> {
            service.reserveStock(OrderNo.of("ORDER-3"), List.of(new InventoryReservationItemDTO(101L, 5)));
            InventoryReservationResult firstRelease =
                    service.releaseReservedStock(OrderNo.of("ORDER-3"), InventoryReleaseReason.USER_CANCELLED);
            InventoryReservationResult secondRelease =
                    service.releaseReservedStock(OrderNo.of("ORDER-3"), InventoryReleaseReason.USER_CANCELLED);
            InventoryStockDTO stock = queryService.getAvailableStock(SkuId.of(101L));

            assertEquals(InventoryReservationStatus.RELEASED.value(), firstRelease.getReservationStatus());
            assertEquals(InventoryReservationStatus.RELEASED.value(), secondRelease.getReservationStatus());
            assertEquals(0, stock.getReservedQuantity());
            assertEquals(100, stock.getAvailableQuantity());
            assertEquals(
                    2, queryService.listLedgersByOrderNo(OrderNo.of("ORDER-3")).size());
            assertEquals(
                    InventoryAuditActionType.RELEASE.value(),
                    queryService
                            .listAuditLogsByOrderNo(OrderNo.of("ORDER-3"))
                            .get(1)
                            .getActionType());
        });
    }

    @Test
    void deductReservedStockShouldBeIdempotent() {
        TestInventoryRepository repository = new TestInventoryRepository();
        InventoryOperationLogSupport operationLogService =
                new InventoryOperationLogSupport(repository, repository, ID_GENERATOR);
        InventoryApplicationService service = new InventoryApplicationService(
                new InventoryReservationApplicationService(
                        repository, repository, operationLogService, RESERVATION_NO_GENERATOR, ID_GENERATOR),
                new InventoryReleaseApplicationService(repository, repository, operationLogService),
                new InventoryDeductionApplicationService(repository, repository, operationLogService));
        InventoryQueryApplicationService queryService =
                new InventoryQueryApplicationService(repository, repository, repository, repository);

        BaconContextHolder.runWithTenantId(1001L, () -> {
            service.reserveStock(OrderNo.of("ORDER-4"), List.of(new InventoryReservationItemDTO(101L, 7)));
            InventoryReservationResult firstDeduct = service.deductReservedStock(OrderNo.of("ORDER-4"));
            InventoryReservationResult secondDeduct = service.deductReservedStock(OrderNo.of("ORDER-4"));
            InventoryStockDTO stock = queryService.getAvailableStock(SkuId.of(101L));

            assertEquals(InventoryReservationStatus.DEDUCTED.value(), firstDeduct.getReservationStatus());
            assertEquals(InventoryReservationStatus.DEDUCTED.value(), secondDeduct.getReservationStatus());
            assertEquals(0, stock.getReservedQuantity());
            assertEquals(93, stock.getOnHandQuantity());
            assertEquals(93, stock.getAvailableQuantity());
            assertEquals(
                    2, queryService.listLedgersByOrderNo(OrderNo.of("ORDER-4")).size());
            assertEquals(
                    InventoryLedgerType.DEDUCT.value(),
                    queryService
                            .listLedgersByOrderNo(OrderNo.of("ORDER-4"))
                            .get(1)
                            .getLedgerType());
        });
    }

    @Test
    void reserveStockShouldBatchLoadInventoriesWithoutPerSkuPreRead() {
        TestInventoryRepository repository = new TestInventoryRepository();
        InventoryOperationLogSupport operationLogService =
                new InventoryOperationLogSupport(repository, repository, ID_GENERATOR);
        InventoryApplicationService service = new InventoryApplicationService(
                new InventoryReservationApplicationService(
                        repository, repository, operationLogService, RESERVATION_NO_GENERATOR, ID_GENERATOR),
                new InventoryReleaseApplicationService(repository, repository, operationLogService),
                new InventoryDeductionApplicationService(repository, repository, operationLogService));

        BaconContextHolder.runWithTenantId(
                1001L,
                () -> service.reserveStock(
                        OrderNo.of("ORDER-5"),
                        List.of(new InventoryReservationItemDTO(101L, 2), new InventoryReservationItemDTO(101L, 3))));

        assertEquals(1, repository.getBatchFindInventoriesCallCount());
        assertEquals(0, repository.getSingleFindInventoryCallCount());
    }

    private static final class TestInventoryRepository
            implements InventoryStockRepository,
                    InventoryReservationRepository,
                    InventoryAuditRecordRepository,
                    InventoryAuditOutboxRepository,
                    InventoryAuditDeadLetterRepository {

        private final Map<String, Inventory> inventories = new ConcurrentHashMap<>();
        private final Map<String, InventoryReservation> reservations = new ConcurrentHashMap<>();
        private final Map<String, List<InventoryLedger>> ledgers = new ConcurrentHashMap<>();
        private final Map<String, List<InventoryAuditLog>> auditLogs = new ConcurrentHashMap<>();
        private int singleFindInventoryCallCount;
        private int batchFindInventoriesCallCount;

        private TestInventoryRepository() {
            inventories.put(
                    key(1001L, 101L),
                    Inventory.reconstruct(
                            InventoryId.of(1L),
                            SkuId.of(101L),
                            WarehouseCode.of("DEFAULT"),
                            new OnHandQuantity(100),
                            new ReservedQuantity(0),
                            InventoryStatus.ENABLED,
                            new Version(0L),
                            Instant.now()));
        }

        @Override
        public Optional<Inventory> findBySkuId(SkuId skuId) {
            singleFindInventoryCallCount++;
            return Optional.ofNullable(inventories.values().stream()
                    .filter(inventory -> java.util.Objects.equals(inventory.getSkuId(), skuId))
                    .findFirst()
                    .orElse(null));
        }

        @Override
        public List<Inventory> list() {
            return inventories.values().stream().toList();
        }

        @Override
        public List<Inventory> listBySkuIds(Set<SkuId> skuIds) {
            batchFindInventoriesCallCount++;
            return inventories.values().stream()
                    .filter(inventory -> skuIds.contains(inventory.getSkuId()))
                    .filter(java.util.Objects::nonNull)
                    .toList();
        }

        @Override
        public List<Inventory> page(SkuId skuId, InventoryStatus status, int pageNo, int pageSize) {
            return list().stream()
                    .filter(inventory -> skuId == null || java.util.Objects.equals(inventory.getSkuId(), skuId))
                    .filter(inventory -> status == null || status.equals(inventory.getStatus()))
                    .skip((long) (pageNo - 1) * pageSize)
                    .limit(pageSize)
                    .toList();
        }

        @Override
        public long count(SkuId skuId, InventoryStatus status) {
            return list().stream()
                    .filter(inventory -> skuId == null || java.util.Objects.equals(inventory.getSkuId(), skuId))
                    .filter(inventory -> status == null || status.equals(inventory.getStatus()))
                    .count();
        }

        @Override
        public Inventory insert(Inventory inventory) {
            Version version = inventory.getVersion() == null
                    ? new Version(0L)
                    : inventory.getVersion().next();
            inventory.markPersisted(version);
            inventories.put(
                    key(
                            1001L,
                            inventory.getSkuId() == null
                                    ? null
                                    : inventory.getSkuId().value()),
                    inventory);
            return inventory;
        }

        @Override
        public Inventory update(Inventory inventory) {
            Version version = inventory.getVersion() == null
                    ? new Version(0L)
                    : inventory.getVersion().next();
            inventory.markPersisted(version);
            inventories.put(
                    key(
                            1001L,
                            inventory.getSkuId() == null
                                    ? null
                                    : inventory.getSkuId().value()),
                    inventory);
            return inventory;
        }

        @Override
        public InventoryReservation insert(InventoryReservation reservation) {
            reservations.put(
                    reservationKey(
                            BaconContextHolder.currentTenantId(),
                            reservation.getOrderNo() == null
                                    ? null
                                    : reservation.getOrderNo().value()),
                    reservation);
            return reservation;
        }

        @Override
        public InventoryReservation update(InventoryReservation reservation) {
            reservations.put(
                    reservationKey(
                            BaconContextHolder.currentTenantId(),
                            reservation.getOrderNo() == null
                                    ? null
                                    : reservation.getOrderNo().value()),
                    reservation);
            return reservation;
        }

        @Override
        public Optional<InventoryReservation> findByOrderNo(OrderNo orderNo) {
            return Optional.ofNullable(reservations.get(
                    reservationKey(BaconContextHolder.currentTenantId(), orderNo == null ? null : orderNo.value())));
        }

        @Override
        public void insertLedger(InventoryLedger ledger) {
            ledgers.computeIfAbsent(
                            reservationKey(
                                    BaconContextHolder.currentTenantId(),
                                    ledger.getOrderNo() == null
                                            ? null
                                            : ledger.getOrderNo().value()),
                            ignored -> new java.util.ArrayList<>())
                    .add(ledger);
        }

        @Override
        public List<InventoryLedger> listLedgers(OrderNo orderNo) {
            return List.copyOf(ledgers.getOrDefault(
                    reservationKey(BaconContextHolder.currentTenantId(), orderNo == null ? null : orderNo.value()),
                    List.of()));
        }

        @Override
        public void insertLog(InventoryAuditLog auditLog) {
            auditLogs
                    .computeIfAbsent(
                            reservationKey(
                                    BaconContextHolder.currentTenantId(),
                                    auditLog.getOrderNo() == null
                                            ? null
                                            : auditLog.getOrderNo().value()),
                            ignored -> new java.util.ArrayList<>())
                    .add(auditLog);
        }

        @Override
        public List<InventoryAuditLog> listLogs(OrderNo orderNo) {
            return List.copyOf(auditLogs.getOrDefault(
                    reservationKey(BaconContextHolder.currentTenantId(), orderNo == null ? null : orderNo.value()),
                    List.of()));
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
