package com.github.thundax.bacon.inventory.application;

import com.github.thundax.bacon.inventory.application.audit.*;
import com.github.thundax.bacon.inventory.application.assembler.*;
import com.github.thundax.bacon.inventory.application.command.*;
import com.github.thundax.bacon.inventory.application.query.*;
import com.github.thundax.bacon.inventory.application.support.*;
import com.github.thundax.bacon.inventory.api.dto.InventoryPageQueryDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryPageResultDTO;
import com.github.thundax.bacon.inventory.domain.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.entity.InventoryLedger;
import com.github.thundax.bacon.inventory.domain.entity.InventoryReservation;
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

class InventoryQueryServiceTest {

    @Test
    void pageInventoriesShouldFilterAndPaginate() {
        TestInventoryRepository repository = new TestInventoryRepository();
        InventoryQueryService service = new InventoryQueryService(repository, repository, repository, repository);

        InventoryPageResultDTO result = service.pageInventories(new InventoryPageQueryDTO(1001L, null,
                Inventory.STATUS_ENABLED, 1, 2));

        assertEquals(2, result.getRecords().size());
        assertEquals(3, result.getTotal());
        assertEquals(101L, result.getRecords().get(0).getSkuId());
        assertEquals(103L, result.getRecords().get(1).getSkuId());
    }

    @Test
    void pageInventoriesShouldUseDefaultPagingValues() {
        TestInventoryRepository repository = new TestInventoryRepository();
        InventoryQueryService service = new InventoryQueryService(repository, repository, repository, repository);

        InventoryPageResultDTO result = service.pageInventories(new InventoryPageQueryDTO(1001L, 104L,
                null, 0, 0));

        assertEquals(1, result.getRecords().size());
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getPageNo());
        assertEquals(20, result.getPageSize());
    }

    private static final class TestInventoryRepository implements InventoryStockRepository, InventoryReservationRepository,
            InventoryLogRepository {

        private final Map<String, Inventory> inventories = new ConcurrentHashMap<>();

        private TestInventoryRepository() {
            inventories.put(key(1001L, 101L), new Inventory(1L, 1001L, 101L, 1L, 100, 0, 100,
                    Inventory.STATUS_ENABLED, 0L, Instant.now()));
            inventories.put(key(1001L, 102L), new Inventory(2L, 1001L, 102L, 1L, 80, 0, 80,
                    Inventory.STATUS_DISABLED, 0L, Instant.now()));
            inventories.put(key(1001L, 103L), new Inventory(3L, 1001L, 103L, 1L, 60, 0, 60,
                    Inventory.STATUS_ENABLED, 0L, Instant.now()));
            inventories.put(key(1001L, 104L), new Inventory(4L, 1001L, 104L, 1L, 40, 0, 40,
                    Inventory.STATUS_ENABLED, 0L, Instant.now()));
        }

        @Override
        public Optional<Inventory> findInventory(Long tenantId, Long skuId) {
            return Optional.ofNullable(inventories.get(key(tenantId, skuId)));
        }

        @Override
        public List<Inventory> findInventories(Long tenantId) {
            return inventories.values().stream()
                    .filter(inventory -> inventory.getTenantId().equals(tenantId))
                    .sorted(java.util.Comparator.comparing(Inventory::getSkuId))
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
                    .filter(inventory -> skuId == null || inventory.getSkuId().equals(skuId))
                    .filter(inventory -> status == null || status.equals(inventory.getStatus()))
                    .skip((long) (pageNo - 1) * pageSize)
                    .limit(pageSize)
                    .toList();
        }

        @Override
        public long countInventories(Long tenantId, Long skuId, String status) {
            return findInventories(tenantId).stream()
                    .filter(inventory -> skuId == null || inventory.getSkuId().equals(skuId))
                    .filter(inventory -> status == null || status.equals(inventory.getStatus()))
                    .count();
        }

        @Override
        public Inventory saveInventory(Inventory inventory) {
            Long version = inventory.getVersion() == null ? 0L : inventory.getVersion() + 1L;
            inventory.markPersisted(version);
            inventories.put(key(inventory.getTenantId(), inventory.getSkuId()), inventory);
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
    }
}
