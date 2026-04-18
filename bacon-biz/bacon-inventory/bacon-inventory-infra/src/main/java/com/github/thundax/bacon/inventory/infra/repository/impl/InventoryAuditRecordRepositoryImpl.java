package com.github.thundax.bacon.inventory.infra.repository.impl;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryLedger;
import com.github.thundax.bacon.inventory.domain.repository.InventoryAuditRecordRepository;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
public class InventoryAuditRecordRepositoryImpl implements InventoryAuditRecordRepository {

    private final InventoryRepositorySupport support;

    public InventoryAuditRecordRepositoryImpl(InventoryRepositorySupport support) {
        this.support = support;
    }

    @Override
    public void insertLedger(InventoryLedger ledger) {
        support.insertLedger(ledger);
    }

    @Override
    public List<InventoryLedger> listLedgers(OrderNo orderNo) {
        return support.listLedgers(orderNo);
    }

    @Override
    public void insertLog(InventoryAuditLog auditLog) {
        support.insertLog(auditLog);
    }

    @Override
    public List<InventoryAuditLog> listLogs(OrderNo orderNo) {
        return support.listLogs(orderNo);
    }
}
