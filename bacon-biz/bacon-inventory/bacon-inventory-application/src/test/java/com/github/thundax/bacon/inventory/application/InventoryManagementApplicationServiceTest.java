package com.github.thundax.bacon.inventory.application;

import com.github.thundax.bacon.inventory.application.command.InventoryManagementApplicationService;
import com.github.thundax.bacon.inventory.api.dto.InventoryStockDTO;
import com.github.thundax.bacon.inventory.domain.model.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryLedger;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservation;
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

class InventoryManagementApplicationServiceTest {

    @Test
    void createInventoryShouldInitializeAvailableQuantity() {
        TestInventoryRepository repository = new TestInventoryRepository();
        InventoryManagementApplicationService service = new InventoryManagementApplicationService(repository);

        InventoryStockDTO result = service.createInventory(1001L, 103L, 30, Inventory.STATUS_ENABLED);

        assertEquals(103L, result.getSkuId());
        assertEquals(30, result.getOnHandQuantity());
        assertEquals(0, result.getReservedQuantity());
        assertEquals(30, result.getAvailableQuantity());
        assertEquals(Inventory.STATUS_ENABLED, result.getStatus());
    }

    @Test
    void updateInventoryStatusShouldPersistStatus() {
        TestInventoryRepository repository = new TestInventoryRepository();
        InventoryManagementApplicationService service = new InventoryManagementApplicationService(repository);

        InventoryStockDTO result = service.updateInventoryStatus(1001L, 101L, Inventory.STATUS_DISABLED);

        assertEquals(Inventory.STATUS_DISABLED, result.getStatus());
        assertEquals(Inventory.STATUS_DISABLED, repository.findInventory(1001L, 101L).orElseThrow().getStatus().value());
    }

    private static final class TestInventoryRepository implements InventoryStockRepository, InventoryReservationRepository,
            InventoryLogRepository {

        private final Map<String, Inventory> inventories = new ConcurrentHashMap<>();

        private TestInventoryRepository() {
            inventories.put(key(1001L, 101L), new Inventory(1L, 1001L, 101L, 1L, 100, 0, 100,
                    Inventory.STATUS_ENABLED, 0L, Instant.now()));
        }

        @Override
        public Optional<Inventory> findInventory(Long tenantId, Long skuId) {
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
            return skuIds.stream().map(skuId -> inventories.get(key(tenantId, skuId)))
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
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<InventoryReservation> findReservation(Long tenantId, String orderNo) {
            return Optional.empty();
        }

        @Override
        public void saveLedger(InventoryLedger ledger) {
        }

        @Override
        public List<InventoryLedger> findLedgers(Long tenantId, String orderNo) {
            return List.of();
        }

        @Override
        public void saveAuditLog(InventoryAuditLog auditLog) {
        }

        @Override
        public List<InventoryAuditLog> findAuditLogs(Long tenantId, String orderNo) {
            return List.of();
        }

        private static String key(Long tenantId, Long skuId) {
            return tenantId + ":" + skuId;
        }

        private static String key(String tenantId, Long skuId) {
            return tenantId + ":" + skuId;
        }
    }
}
