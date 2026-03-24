package com.github.thundax.bacon.inventory.application.service;

import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.entity.InventoryLedger;
import com.github.thundax.bacon.inventory.domain.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.entity.InventoryReservationItem;
import com.github.thundax.bacon.inventory.domain.repository.InventoryRepository;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
public class InventoryOperationLogService {

    private final InventoryRepository inventoryRepository;

    public InventoryOperationLogService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public void recordReserveSuccess(InventoryReservation reservation, Instant occurredAt) {
        recordLedgerBatch(reservation, reservation.getItems(), InventoryLedger.TYPE_RESERVE, occurredAt);
        recordAuditAfterCommit(reservation, InventoryAuditLog.ACTION_RESERVE, occurredAt);
    }

    public void recordReserveFailed(InventoryReservation reservation, Instant occurredAt) {
        recordAuditAfterCommit(reservation, InventoryAuditLog.ACTION_RESERVE_FAILED, occurredAt);
    }

    public void recordReleaseSuccess(InventoryReservation reservation, Instant occurredAt) {
        recordLedgerBatch(reservation, reservation.getItems(), InventoryLedger.TYPE_RELEASE, occurredAt);
        recordAuditAfterCommit(reservation, InventoryAuditLog.ACTION_RELEASE, occurredAt);
    }

    public void recordDeductSuccess(InventoryReservation reservation, Instant occurredAt) {
        recordLedgerBatch(reservation, reservation.getItems(), InventoryLedger.TYPE_DEDUCT, occurredAt);
        recordAuditAfterCommit(reservation, InventoryAuditLog.ACTION_DEDUCT, occurredAt);
    }

    private void recordLedgerBatch(InventoryReservation reservation, List<InventoryReservationItem> items,
                                   String ledgerType, Instant occurredAt) {
        for (InventoryReservationItem item : items) {
            inventoryRepository.saveLedger(new InventoryLedger(null, reservation.getTenantId(),
                    reservation.getOrderNo(), reservation.getReservationNo(), item.getSkuId(),
                    reservation.getWarehouseId(), ledgerType, item.getQuantity(), occurredAt));
        }
    }

    private void recordAuditAfterCommit(InventoryReservation reservation, String actionType, Instant occurredAt) {
        Runnable task = () -> saveAuditSafely(reservation, actionType, occurredAt);
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            task.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                task.run();
            }
        });
    }

    private void saveAuditSafely(InventoryReservation reservation, String actionType, Instant occurredAt) {
        try {
            inventoryRepository.saveAuditLog(new InventoryAuditLog(null, reservation.getTenantId(),
                reservation.getOrderNo(), reservation.getReservationNo(), actionType,
                InventoryAuditLog.OPERATOR_TYPE_SYSTEM, InventoryAuditLog.OPERATOR_ID_SYSTEM, occurredAt));
        } catch (RuntimeException ex) {
            log.warn("Failed to persist inventory audit log, orderNo={}, actionType={}",
                    reservation.getOrderNo(), actionType, ex);
        }
    }
}
