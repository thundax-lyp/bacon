package com.github.thundax.bacon.inventory.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.commerce.valueobject.WarehouseCode;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.valueobject.Version;
import com.github.thundax.bacon.inventory.application.query.InventoryQueryApplicationService;
import com.github.thundax.bacon.inventory.application.result.InventoryPageResult;
import com.github.thundax.bacon.inventory.domain.model.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryLedger;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.InventoryId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.OnHandQuantity;
import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservedQuantity;
import com.github.thundax.bacon.inventory.domain.repository.InventoryLogRepository;
import com.github.thundax.bacon.inventory.domain.repository.InventoryReservationRepository;
import com.github.thundax.bacon.inventory.domain.repository.InventoryStockRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Test;

class InventoryQueryApplicationServiceTest {

    @Test
    void pageInventoriesShouldFilterAndPaginate() {
        TestInventoryRepository repository = new TestInventoryRepository();
        InventoryQueryApplicationService service =
                new InventoryQueryApplicationService(repository, repository, repository, repository);

        InventoryPageResult result = BaconContextHolder.callWithTenantId(
                1001L, () -> service.pageInventories(null, InventoryStatus.ENABLED, 1, 2));

        assertEquals(2, result.getRecords().size());
        assertEquals(3, result.getTotal());
        assertEquals(101L, result.getRecords().get(0).getSkuId());
        assertEquals(103L, result.getRecords().get(1).getSkuId());
    }

    @Test
    void pageInventoriesShouldUseDefaultPagingValues() {
        TestInventoryRepository repository = new TestInventoryRepository();
        InventoryQueryApplicationService service =
                new InventoryQueryApplicationService(repository, repository, repository, repository);

        InventoryPageResult result =
                BaconContextHolder.callWithTenantId(1001L, () -> service.pageInventories(SkuId.of(104L), null, 0, 0));

        assertEquals(1, result.getRecords().size());
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getPageNo());
        assertEquals(20, result.getPageSize());
    }

    private static final class TestInventoryRepository
            implements InventoryStockRepository, InventoryReservationRepository, InventoryLogRepository {

        private final Map<String, Inventory> inventories = new ConcurrentHashMap<>();

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
            inventories.put(
                    key(1001L, 102L),
                    Inventory.reconstruct(
                            InventoryId.of(2L),
                            SkuId.of(102L),
                            WarehouseCode.of("DEFAULT"),
                            new OnHandQuantity(80),
                            new ReservedQuantity(0),
                            InventoryStatus.DISABLED,
                            new Version(0L),
                            Instant.now()));
            inventories.put(
                    key(1001L, 103L),
                    Inventory.reconstruct(
                            InventoryId.of(3L),
                            SkuId.of(103L),
                            WarehouseCode.of("DEFAULT"),
                            new OnHandQuantity(60),
                            new ReservedQuantity(0),
                            InventoryStatus.ENABLED,
                            new Version(0L),
                            Instant.now()));
            inventories.put(
                    key(1001L, 104L),
                    Inventory.reconstruct(
                            InventoryId.of(4L),
                            SkuId.of(104L),
                            WarehouseCode.of("DEFAULT"),
                            new OnHandQuantity(40),
                            new ReservedQuantity(0),
                            InventoryStatus.ENABLED,
                            new Version(0L),
                            Instant.now()));
        }

        @Override
        public Optional<Inventory> findInventory(SkuId skuId) {
            return Optional.ofNullable(inventories.values().stream()
                    .filter(inventory -> java.util.Objects.equals(inventory.getSkuId(), skuId))
                    .findFirst()
                    .orElse(null));
        }

        @Override
        public List<Inventory> findInventories() {
            return inventories.values().stream()
                    .sorted(java.util.Comparator.comparing(inventory -> inventory.getSkuId() == null
                            ? null
                            : inventory.getSkuId().value()))
                    .toList();
        }

        @Override
        public List<Inventory> findInventories(Set<SkuId> skuIds) {
            return skuIds.stream()
                    .map(this::findInventory)
                    .flatMap(Optional::stream)
                    .toList();
        }

        @Override
        public List<Inventory> pageInventories(SkuId skuId, InventoryStatus status, int pageNo, int pageSize) {
            return findInventories().stream()
                    .filter(inventory -> skuId == null || java.util.Objects.equals(inventory.getSkuId(), skuId))
                    .filter(inventory -> status == null || status.equals(inventory.getStatus()))
                    .skip((long) (pageNo - 1) * pageSize)
                    .limit(pageSize)
                    .toList();
        }

        @Override
        public long countInventories(SkuId skuId, InventoryStatus status) {
            return findInventories().stream()
                    .filter(inventory -> skuId == null || java.util.Objects.equals(inventory.getSkuId(), skuId))
                    .filter(inventory -> status == null || status.equals(inventory.getStatus()))
                    .count();
        }

        @Override
        public Inventory saveInventory(Inventory inventory) {
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
        public InventoryReservation saveReservation(InventoryReservation reservation) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<InventoryReservation> findReservation(OrderNo orderNo) {
            return Optional.empty();
        }

        @Override
        public void saveLedger(InventoryLedger ledger) {}

        @Override
        public List<InventoryLedger> findLedgers(OrderNo orderNo) {
            return List.of();
        }

        @Override
        public void saveAuditLog(InventoryAuditLog auditLog) {}

        @Override
        public List<InventoryAuditLog> findAuditLogs(OrderNo orderNo) {
            return List.of();
        }

        private static String key(Long tenantId, Long skuId) {
            return tenantId + ":" + skuId;
        }
    }
}
