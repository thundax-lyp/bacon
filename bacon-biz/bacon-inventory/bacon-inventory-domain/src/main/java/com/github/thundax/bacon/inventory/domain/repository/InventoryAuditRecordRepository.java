package com.github.thundax.bacon.inventory.domain.repository;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryLedger;
import java.util.List;

public interface InventoryAuditRecordRepository {

    void insertLedger(InventoryLedger ledger);

    List<InventoryLedger> findLedgers(OrderNo orderNo);

    void insertAuditLog(InventoryAuditLog auditLog);

    List<InventoryAuditLog> findAuditLogs(OrderNo orderNo);
}
