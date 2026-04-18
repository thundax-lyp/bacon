package com.github.thundax.bacon.inventory.infra.repository.impl;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryLedger;
import com.github.thundax.bacon.inventory.domain.repository.InventoryAuditRecordRepository;
import java.util.List;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Primary
@Profile("test")
public class InMemoryInventoryAuditRecordRepositoryImpl implements InventoryAuditRecordRepository {

    private final InMemoryInventoryRepositorySupport support;

    public InMemoryInventoryAuditRecordRepositoryImpl(InMemoryInventoryRepositorySupport support) {
        this.support = support;
    }

    @Override
    public void insertLedger(InventoryLedger ledger) {
        support.insertLedger(ledger);
    }

    @Override
    public List<InventoryLedger> findLedgers(OrderNo orderNo) {
        return support.findLedgers(orderNo);
    }

    @Override
    public void insertAuditLog(InventoryAuditLog auditLog) {
        support.insertAuditLog(auditLog);
    }

    @Override
    public List<InventoryAuditLog> findAuditLogs(OrderNo orderNo) {
        return support.findAuditLogs(orderNo);
    }
}
