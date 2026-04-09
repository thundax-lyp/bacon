package com.github.thundax.bacon.inventory.application;

import com.github.thundax.bacon.common.id.domain.SkuId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.application.query.InventoryQueryApplicationService;
import com.github.thundax.bacon.inventory.api.dto.InventoryPageResultDTO;
import com.github.thundax.bacon.inventory.domain.model.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryLedger;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.InventoryId;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.commerce.valueobject.WarehouseCode;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

class InventoryQueryApplicationServiceTest {

    @Test
    void pageInventoriesShouldFilterAndPaginate() {
        TestInventoryRepository repository = new TestInventoryRepository();
        InventoryQueryApplicationService service = new InventoryQueryApplicationService(repository, repository, repository, repository);

        InventoryPageResultDTO result = service.pageInventories(TenantId.of(1001L), null,
                InventoryStatus.ENABLED, 1, 2);

        assertEquals(2, result.getRecords().size());
        assertEquals(3, result.getTotal());
        assertEquals(101L, result.getRecords().get(0).getSkuId());
        assertEquals(103L, result.getRecords().get(1).getSkuId());
    }

    @Test
    void pageInventoriesShouldUseDefaultPagingValues() {
        TestInventoryRepository repository = new TestInventoryRepository();
        InventoryQueryApplicationService service = new InventoryQueryApplicationService(repository, repository, repository, repository);

        InventoryPageResultDTO result = service.pageInventories(TenantId.of(1001L), SkuId.of(104L),
                null, 0, 0);

        assertEquals(1, result.getRecords().size());
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getPageNo());
        assertEquals(20, result.getPageSize());
    }

    private static final class TestInventoryRepository implements InventoryStockRepository, InventoryReservationRepository,
            InventoryLogRepository {

        private final Map<String, Inventory> inventories = new ConcurrentHashMap<>();

        private TestInventoryRepository() {
            inventories.put(key(1001L, 101L), Inventory.reconstruct(
                    InventoryId.of(1L),
                    TenantId.of(1001L),
                    SkuId.of(101L),
                    WarehouseCode.of("DEFAULT"),
                    100,
                    0,
                    100,
                    InventoryStatus.ENABLED,
                    0L,
                    Instant.now()));
            inventories.put(key(1001L, 102L), Inventory.reconstruct(
                    InventoryId.of(2L),
                    TenantId.of(1001L),
                    SkuId.of(102L),
                    WarehouseCode.of("DEFAULT"),
                    80,
                    0,
                    80,
                    InventoryStatus.DISABLED,
                    0L,
                    Instant.now()));
            inventories.put(key(1001L, 103L), Inventory.reconstruct(
                    InventoryId.of(3L),
                    TenantId.of(1001L),
                    SkuId.of(103L),
                    WarehouseCode.of("DEFAULT"),
                    60,
                    0,
                    60,
                    InventoryStatus.ENABLED,
                    0L,
                    Instant.now()));
            inventories.put(key(1001L, 104L), Inventory.reconstruct(
                    InventoryId.of(4L),
                    TenantId.of(1001L),
                    SkuId.of(104L),
                    WarehouseCode.of("DEFAULT"),
                    40,
                    0,
                    40,
                    InventoryStatus.ENABLED,
                    0L,
                    Instant.now()));
        }

        @Override
        public Optional<Inventory> findInventory(TenantId tenantId, SkuId skuId) {
            return Optional.ofNullable(inventories.get(key(tenantId == null ? null : tenantId.value(),
                    skuId == null ? null : skuId.value())));
        }

        @Override
        public List<Inventory> findInventories(TenantId tenantId) {
            return inventories.values().stream()
                    .filter(inventory -> java.util.Objects.equals(inventory.getTenantId(), tenantId))
                    .sorted(java.util.Comparator.comparing(inventory -> inventory.getSkuId() == null ? null : inventory.getSkuId().value()))
                    .toList();
        }

        @Override
        public List<Inventory> findInventories(TenantId tenantId, Set<SkuId> skuIds) {
            return skuIds.stream().map(skuId -> inventories.get(key(tenantId == null ? null : tenantId.value(),
                            skuId == null ? null : skuId.value())))
                    .filter(java.util.Objects::nonNull)
                    .toList();
        }

        @Override
        public List<Inventory> pageInventories(TenantId tenantId, SkuId skuId, InventoryStatus status, int pageNo, int pageSize) {
            return findInventories(tenantId).stream()
                    .filter(inventory -> skuId == null || java.util.Objects.equals(inventory.getSkuId(), skuId))
                    .filter(inventory -> status == null || status.equals(inventory.getStatus()))
                    .skip((long) (pageNo - 1) * pageSize)
                    .limit(pageSize)
                    .toList();
        }

        @Override
        public long countInventories(TenantId tenantId, SkuId skuId, InventoryStatus status) {
            return findInventories(tenantId).stream()
                    .filter(inventory -> skuId == null || java.util.Objects.equals(inventory.getSkuId(), skuId))
                    .filter(inventory -> status == null || status.equals(inventory.getStatus()))
                    .count();
        }

        @Override
        public Inventory saveInventory(Inventory inventory) {
            Long version = inventory.getVersion() == null ? 0L : inventory.getVersion() + 1L;
            inventory.markPersisted(version);
            inventories.put(key(inventory.getTenantId() == null ? null : inventory.getTenantId().value(),
                    inventory.getSkuId() == null ? null : inventory.getSkuId().value()), inventory);
            return inventory;
        }

        @Override
        public InventoryReservation saveReservation(InventoryReservation reservation) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<InventoryReservation> findReservation(TenantId tenantId, OrderNo orderNo) {
            return Optional.empty();
        }

        @Override
        public void saveLedger(InventoryLedger ledger) {
        }

        @Override
        public List<InventoryLedger> findLedgers(TenantId tenantId, OrderNo orderNo) {
            return List.of();
        }

        @Override
        public void saveAuditLog(InventoryAuditLog auditLog) {
        }

        @Override
        public List<InventoryAuditLog> findAuditLogs(TenantId tenantId, OrderNo orderNo) {
            return List.of();
        }

        private static String key(Long tenantId, Long skuId) {
            return tenantId + ":" + skuId;
        }

    }
}
