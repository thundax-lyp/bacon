package com.github.thundax.bacon.inventory.infra.repository.impl;

import com.github.thundax.bacon.inventory.domain.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.entity.InventoryLedger;
import com.github.thundax.bacon.inventory.domain.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.entity.InventoryReservationItem;
import com.github.thundax.bacon.inventory.domain.repository.InventoryLogRepository;
import com.github.thundax.bacon.inventory.domain.repository.InventoryReservationRepository;
import com.github.thundax.bacon.inventory.domain.repository.InventoryStockRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Repository
@ConditionalOnProperty(name = "bacon.inventory.in-memory.enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnMissingBean(InventoryStockRepository.class)
public class InMemoryInventoryRepositoryImpl implements InventoryStockRepository, InventoryReservationRepository,
        InventoryLogRepository {

    private final AtomicLong inventoryIdGenerator = new AtomicLong(1000L);
    private final AtomicLong reservationIdGenerator = new AtomicLong(1000L);
    private final AtomicLong itemIdGenerator = new AtomicLong(1000L);
    private final AtomicLong ledgerIdGenerator = new AtomicLong(1000L);
    private final AtomicLong auditLogIdGenerator = new AtomicLong(1000L);
    private final Map<String, Inventory> inventories = new ConcurrentHashMap<>();
    private final Map<String, InventoryReservation> reservations = new ConcurrentHashMap<>();
    private final Map<String, List<InventoryLedger>> ledgers = new ConcurrentHashMap<>();
    private final Map<String, List<InventoryAuditLog>> auditLogs = new ConcurrentHashMap<>();

    public InMemoryInventoryRepositoryImpl() {
        log.info("Using in-memory inventory repository");
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
        return skuIds.stream()
                .map(skuId -> inventories.get(key(tenantId, skuId)))
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
        if (inventory.getId() == null) {
            inventory = new Inventory(inventoryIdGenerator.getAndIncrement(), inventory.getTenantId(), inventory.getSkuId(),
                    inventory.getWarehouseId(), inventory.getOnHandQuantity(), inventory.getReservedQuantity(),
                    inventory.getAvailableQuantity(), inventory.getStatus(), inventory.getVersion(), inventory.getUpdatedAt());
        }
        Long version = inventory.getVersion() == null ? 0L : inventory.getVersion() + 1L;
        inventory.markPersisted(version);
        inventories.put(key(inventory.getTenantId(), inventory.getSkuId()), inventory);
        return inventory;
    }

    @Override
    public InventoryReservation saveReservation(InventoryReservation reservation) {
        if (reservation.getId() == null) {
            reservation = InventoryReservation.rehydrate(reservationIdGenerator.getAndIncrement(), reservation.getTenantId(),
                    reservation.getReservationNo(), reservation.getOrderNo(), reservation.getWarehouseId(),
                    reservation.getCreatedAt(), reservation.getItems().stream()
                            .map(item -> new InventoryReservationItem(
                                    item.getId() == null ? itemIdGenerator.getAndIncrement() : item.getId(),
                                    item.getTenantId(), item.getReservationNo(), item.getSkuId(), item.getQuantity()))
                            .toList(),
                    reservation.getReservationStatus(), reservation.getFailureReason(), reservation.getReleaseReason(),
                    reservation.getReleasedAt(), reservation.getDeductedAt());
        }
        reservations.put(reservationKey(reservation.getTenantId(), reservation.getOrderNo()), reservation);
        return reservation;
    }

    @Override
    public Optional<InventoryReservation> findReservation(Long tenantId, String orderNo) {
        return Optional.ofNullable(reservations.get(reservationKey(tenantId, orderNo)));
    }

    @Override
    public void saveLedger(InventoryLedger ledger) {
        if (ledger.getId() == null) {
            ledger = new InventoryLedger(ledgerIdGenerator.getAndIncrement(), ledger.getTenantId(), ledger.getOrderNo(),
                    ledger.getReservationNo(), ledger.getSkuId(), ledger.getWarehouseId(), ledger.getLedgerType(),
                    ledger.getQuantity(), ledger.getOccurredAt());
        }
        ledgers.computeIfAbsent(reservationKey(ledger.getTenantId(), ledger.getOrderNo()), key -> new ArrayList<>())
                .add(ledger);
    }

    @Override
    public List<InventoryLedger> findLedgers(Long tenantId, String orderNo) {
        return List.copyOf(ledgers.getOrDefault(reservationKey(tenantId, orderNo), List.of()));
    }

    @Override
    public void saveAuditLog(InventoryAuditLog auditLog) {
        if (auditLog.getId() == null) {
            auditLog = new InventoryAuditLog(auditLogIdGenerator.getAndIncrement(), auditLog.getTenantId(),
                    auditLog.getOrderNo(), auditLog.getReservationNo(), auditLog.getActionType(),
                    auditLog.getOperatorType(), auditLog.getOperatorId(), auditLog.getOccurredAt());
        }
        auditLogs.computeIfAbsent(reservationKey(auditLog.getTenantId(), auditLog.getOrderNo()), key -> new ArrayList<>())
                .add(auditLog);
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
