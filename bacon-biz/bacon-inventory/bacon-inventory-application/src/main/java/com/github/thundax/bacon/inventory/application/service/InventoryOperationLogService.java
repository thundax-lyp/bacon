package com.github.thundax.bacon.inventory.application.service;

import com.github.thundax.bacon.inventory.domain.entity.Inventory;
import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.entity.InventoryLedger;
import com.github.thundax.bacon.inventory.domain.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.entity.InventoryReservationItem;
import com.github.thundax.bacon.inventory.domain.repository.InventoryRepository;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class InventoryOperationLogService {

    private final AtomicLong idGenerator = new AtomicLong(1L);
    private final InventoryRepository inventoryRepository;

    public InventoryOperationLogService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public void recordReserveSuccess(InventoryReservation reservation, Instant occurredAt) {
        recordLedgerBatch(reservation, reservation.getItems(), InventoryLedger.TYPE_RESERVE, occurredAt);
        recordAudit(reservation, InventoryAuditLog.ACTION_RESERVE, occurredAt);
    }

    public void recordReserveFailed(InventoryReservation reservation, Instant occurredAt) {
        recordAudit(reservation, InventoryAuditLog.ACTION_RESERVE_FAILED, occurredAt);
    }

    public void recordReleaseSuccess(InventoryReservation reservation, Instant occurredAt) {
        recordLedgerBatch(reservation, reservation.getItems(), InventoryLedger.TYPE_RELEASE, occurredAt);
        recordAudit(reservation, InventoryAuditLog.ACTION_RELEASE, occurredAt);
    }

    public void recordDeductSuccess(InventoryReservation reservation, Instant occurredAt) {
        recordLedgerBatch(reservation, reservation.getItems(), InventoryLedger.TYPE_DEDUCT, occurredAt);
        recordAudit(reservation, InventoryAuditLog.ACTION_DEDUCT, occurredAt);
    }

    private void recordLedgerBatch(InventoryReservation reservation, List<InventoryReservationItem> items,
                                   String ledgerType, Instant occurredAt) {
        for (InventoryReservationItem item : items) {
            inventoryRepository.saveLedger(new InventoryLedger(idGenerator.getAndIncrement(), reservation.getTenantId(),
                    reservation.getOrderNo(), reservation.getReservationNo(), item.getSkuId(),
                    reservation.getWarehouseId(), ledgerType, item.getQuantity(), occurredAt));
        }
    }

    private void recordAudit(InventoryReservation reservation, String actionType, Instant occurredAt) {
        inventoryRepository.saveAuditLog(new InventoryAuditLog(idGenerator.getAndIncrement(), reservation.getTenantId(),
                reservation.getOrderNo(), reservation.getReservationNo(), actionType,
                InventoryAuditLog.OPERATOR_TYPE_SYSTEM, InventoryAuditLog.OPERATOR_ID_SYSTEM, occurredAt));
    }
}
