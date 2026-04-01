package com.github.thundax.bacon.inventory.infra.repository.impl;

import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryLedger;
import com.github.thundax.bacon.inventory.domain.repository.InventoryAuditRecordRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class InventoryAuditRecordRepositoryImpl implements InventoryAuditRecordRepository {

    private final InventoryRepositorySupport support;

    public InventoryAuditRecordRepositoryImpl(InventoryRepositorySupport support) {
        this.support = support;
    }

    @Override
    public void saveLedger(InventoryLedger ledger) {
        support.saveLedger(ledger);
    }

    @Override
    public List<InventoryLedger> findLedgers(Long tenantId, String orderNo) {
        return support.findLedgers(tenantId, orderNo);
    }

    @Override
    public void saveAuditLog(InventoryAuditLog auditLog) {
        support.saveAuditLog(auditLog);
    }

    @Override
    public List<InventoryAuditLog> findAuditLogs(Long tenantId, String orderNo) {
        return support.findAuditLogs(tenantId, orderNo);
    }
}
