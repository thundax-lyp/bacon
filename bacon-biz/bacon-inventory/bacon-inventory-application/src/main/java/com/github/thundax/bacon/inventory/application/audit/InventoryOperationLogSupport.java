package com.github.thundax.bacon.inventory.application.audit;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditOutbox;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryLedger;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservationItem;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditActionType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditOperatorType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditOutboxStatus;
import com.github.thundax.bacon.inventory.domain.repository.InventoryAuditOutboxRepository;
import com.github.thundax.bacon.inventory.domain.repository.InventoryAuditRecordRepository;
import io.micrometer.core.instrument.Metrics;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
public class InventoryOperationLogSupport {

    private final InventoryAuditRecordRepository inventoryAuditRecordRepository;
    private final InventoryAuditOutboxRepository inventoryAuditOutboxRepository;

    public InventoryOperationLogSupport(InventoryAuditRecordRepository inventoryAuditRecordRepository,
                                        InventoryAuditOutboxRepository inventoryAuditOutboxRepository) {
        this.inventoryAuditRecordRepository = inventoryAuditRecordRepository;
        this.inventoryAuditOutboxRepository = inventoryAuditOutboxRepository;
    }

    public void recordReserveSuccess(InventoryReservation reservation, Instant occurredAt) {
        recordLedgerBatch(reservation, reservation.getItems(), InventoryLedger.TYPE_RESERVE, occurredAt);
        recordAuditAfterCommit(reservation, InventoryAuditActionType.RESERVE, occurredAt);
    }

    public void recordReserveFailed(InventoryReservation reservation, Instant occurredAt) {
        recordAuditAfterCommit(reservation, InventoryAuditActionType.RESERVE_FAILED, occurredAt);
    }

    public void recordReleaseSuccess(InventoryReservation reservation, Instant occurredAt) {
        recordLedgerBatch(reservation, reservation.getItems(), InventoryLedger.TYPE_RELEASE, occurredAt);
        recordAuditAfterCommit(reservation, InventoryAuditActionType.RELEASE, occurredAt);
    }

    public void recordDeductSuccess(InventoryReservation reservation, Instant occurredAt) {
        recordLedgerBatch(reservation, reservation.getItems(), InventoryLedger.TYPE_DEDUCT, occurredAt);
        recordAuditAfterCommit(reservation, InventoryAuditActionType.DEDUCT, occurredAt);
    }

    private void recordLedgerBatch(InventoryReservation reservation, List<InventoryReservationItem> items,
                                   String ledgerType, Instant occurredAt) {
        for (InventoryReservationItem item : items) {
            inventoryAuditRecordRepository.saveLedger(new InventoryLedger(null, TenantId.of(reservation.getTenantId()),
                    reservation.getOrderNo(), reservation.getReservationNo(), item.getSkuId(),
                    reservation.getWarehouseNo(), ledgerType, item.getQuantity(), occurredAt));
        }
    }

    private void recordAuditAfterCommit(InventoryReservation reservation, InventoryAuditActionType actionType,
                                        Instant occurredAt) {
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

    private void saveAuditSafely(InventoryReservation reservation, InventoryAuditActionType actionType,
                                 Instant occurredAt) {
        try {
            inventoryAuditRecordRepository.saveAuditLog(new InventoryAuditLog(null, reservation.getTenantId(),
                    reservation.getOrderNo(), reservation.getReservationNo(), actionType.value(),
                    InventoryAuditOperatorType.SYSTEM.value(), InventoryAuditLog.OPERATOR_ID_SYSTEM, occurredAt));
            Metrics.counter("bacon.inventory.audit.write.success.total", "actionType", actionType.value()).increment();
        } catch (RuntimeException ex) {
            Metrics.counter("bacon.inventory.audit.write.fail.total", "actionType", actionType.value()).increment();
            saveAuditOutboxSafely(reservation, actionType, occurredAt, ex);
            log.error("ALERT inventory audit write failed, orderNo={}, reservationNo={}, actionType={}",
                    reservation.getOrderNo(), reservation.getReservationNo(), actionType.value(), ex);
        }
    }

    private void saveAuditOutboxSafely(InventoryReservation reservation, InventoryAuditActionType actionType,
                                       Instant occurredAt, RuntimeException ex) {
        try {
            inventoryAuditOutboxRepository.saveAuditOutbox(new InventoryAuditOutbox(null, reservation.getTenantId(),
                    reservation.getOrderNo(), reservation.getReservationNo(), actionType.value(),
                    InventoryAuditOperatorType.SYSTEM.value(), InventoryAuditLog.OPERATOR_ID_SYSTEM, occurredAt,
                    truncateErrorMessage(ex.getMessage()), InventoryAuditOutboxStatus.NEW, 0, Instant.now(),
                    null, null, null, null, Instant.now(), Instant.now()));
            Metrics.counter("bacon.inventory.audit.outbox.persist.success.total", "actionType", actionType.value())
                    .increment();
        } catch (RuntimeException outboxEx) {
            Metrics.counter("bacon.inventory.audit.outbox.persist.fail.total", "actionType", actionType.value())
                    .increment();
            log.error("ALERT inventory audit outbox persist failed, orderNo={}, reservationNo={}, actionType={}",
                    reservation.getOrderNo(), reservation.getReservationNo(), actionType.value(), outboxEx);
        }
    }

    private String truncateErrorMessage(String message) {
        if (message == null || message.isBlank()) {
            return "UNKNOWN";
        }
        return message.length() <= 512 ? message : message.substring(0, 512);
    }
}
