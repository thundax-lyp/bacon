package com.github.thundax.bacon.inventory.domain.repository;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryLedger;
import com.github.thundax.bacon.inventory.domain.model.valueobject.OrderNo;
import java.util.List;

public interface InventoryAuditRecordRepository {

    void saveLedger(InventoryLedger ledger);

    List<InventoryLedger> findLedgers(TenantId tenantId, OrderNo orderNo);

    void saveAuditLog(InventoryAuditLog auditLog);

    List<InventoryAuditLog> findAuditLogs(TenantId tenantId, OrderNo orderNo);
}
