package com.github.thundax.bacon.inventory.application;

import com.github.thundax.bacon.common.id.domain.SkuId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.application.command.InventoryManagementApplicationService;
import com.github.thundax.bacon.inventory.api.dto.InventoryStockDTO;
import com.github.thundax.bacon.inventory.domain.exception.InventoryDomainException;
import com.github.thundax.bacon.inventory.domain.exception.InventoryErrorCode;
import com.github.thundax.bacon.inventory.domain.model.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryLedger;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.InventoryId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.OrderNo;
import com.github.thundax.bacon.inventory.domain.model.valueobject.WarehouseNo;
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
import static org.junit.jupiter.api.Assertions.assertThrows;

class InventoryManagementApplicationServiceTest {

    @Test
    void createInventoryShouldInitializeAvailableQuantity() {
        TestInventoryRepository repository = new TestInventoryRepository();
        InventoryManagementApplicationService service = new InventoryManagementApplicationService(repository);

        InventoryStockDTO result = service.createInventory(TenantId.of(1001L), SkuId.of(103L), 30, InventoryStatus.ENABLED.value());

        assertEquals(103L, result.getSkuId());
        assertEquals(30, result.getOnHandQuantity());
        assertEquals(0, result.getReservedQuantity());
        assertEquals(30, result.getAvailableQuantity());
        assertEquals(InventoryStatus.ENABLED.value(), result.getStatus());
    }

    @Test
    void updateInventoryStatusShouldPersistStatus() {
        TestInventoryRepository repository = new TestInventoryRepository();
        InventoryManagementApplicationService service = new InventoryManagementApplicationService(repository);

        InventoryStockDTO result = service.updateInventoryStatus(TenantId.of(1001L), SkuId.of(101L), InventoryStatus.DISABLED.value());

        assertEquals(InventoryStatus.DISABLED.value(), result.getStatus());
        assertEquals(InventoryStatus.DISABLED.value(),
                repository.findInventory(TenantId.of(1001L), SkuId.of(101L)).orElseThrow().getStatus().value());
    }

    @Test
    void createInventoryShouldRejectInvalidOnHandQuantityInApplication() {
        TestInventoryRepository repository = new TestInventoryRepository();
        InventoryManagementApplicationService service = new InventoryManagementApplicationService(repository);

        InventoryDomainException exception = assertThrows(InventoryDomainException.class,
                () -> service.createInventory(TenantId.of(1001L), SkuId.of(103L), -1, InventoryStatus.ENABLED.value()));

        assertEquals(InventoryErrorCode.INVALID_ON_HAND_QUANTITY.code(), exception.getCode());
    }

    @Test
    void updateInventoryStatusShouldRejectInvalidStatusInApplication() {
        TestInventoryRepository repository = new TestInventoryRepository();
        InventoryManagementApplicationService service = new InventoryManagementApplicationService(repository);

        InventoryDomainException exception = assertThrows(InventoryDomainException.class,
                () -> service.updateInventoryStatus(TenantId.of(1001L), SkuId.of(101L), "UNKNOWN"));

        assertEquals(InventoryErrorCode.INVALID_INVENTORY_STATUS.code(), exception.getCode());
    }

    private static final class TestInventoryRepository implements InventoryStockRepository, InventoryReservationRepository,
            InventoryLogRepository {

        private final Map<String, Inventory> inventories = new ConcurrentHashMap<>();

        private TestInventoryRepository() {
            inventories.put(key(1001L, 101L), new Inventory(InventoryId.of(1L), TenantId.of(1001L), SkuId.of(101L), WarehouseNo.of("DEFAULT"), 100, 0, 100,
                    InventoryStatus.ENABLED, 0L, Instant.now()));
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
            inventories.put(key(inventory.getTenantId().value(),
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
