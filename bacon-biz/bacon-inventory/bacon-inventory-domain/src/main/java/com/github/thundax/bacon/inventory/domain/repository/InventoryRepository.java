package com.github.thundax.bacon.inventory.domain.repository;

import com.github.thundax.bacon.inventory.domain.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.entity.InventoryLedger;
import com.github.thundax.bacon.inventory.domain.entity.InventoryReservation;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface InventoryRepository {

    Optional<Inventory> findInventory(Long tenantId, Long skuId);

    List<Inventory> findInventories(Long tenantId);

    List<Inventory> findInventories(Long tenantId, Set<Long> skuIds);

    Inventory saveInventory(Inventory inventory);

    InventoryReservation saveReservation(InventoryReservation reservation);

    Optional<InventoryReservation> findReservation(Long tenantId, String orderNo);

    void saveLedger(InventoryLedger ledger);

    List<InventoryLedger> findLedgers(Long tenantId, String orderNo);

    void saveAuditLog(InventoryAuditLog auditLog);

    List<InventoryAuditLog> findAuditLogs(Long tenantId, String orderNo);
}
