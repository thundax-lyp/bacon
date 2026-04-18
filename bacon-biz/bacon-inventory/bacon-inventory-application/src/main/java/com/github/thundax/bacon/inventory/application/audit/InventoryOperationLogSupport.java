package com.github.thundax.bacon.inventory.application.audit;

import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.inventory.application.codec.OrderNoCodec;
import com.github.thundax.bacon.inventory.application.codec.ReservationNoCodec;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditOutbox;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryLedger;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservation;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryReservationItem;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditActionType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditOperatorType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditOutboxStatus;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryLedgerType;
import com.github.thundax.bacon.inventory.domain.model.valueobject.OutboxId;
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

    private static final String LEDGER_ID_BIZ_TAG = "inventory-ledger-id";
    private static final String AUDIT_LOG_ID_BIZ_TAG = "inventory-audit-log-id";
    private static final String AUDIT_OUTBOX_ID_BIZ_TAG = "inventory-audit-outbox-id";

    private final InventoryAuditRecordRepository inventoryAuditRecordRepository;
    private final InventoryAuditOutboxRepository inventoryAuditOutboxRepository;
    private final IdGenerator idGenerator;

    public InventoryOperationLogSupport(
            InventoryAuditRecordRepository inventoryAuditRecordRepository,
            InventoryAuditOutboxRepository inventoryAuditOutboxRepository,
            IdGenerator idGenerator) {
        this.inventoryAuditRecordRepository = inventoryAuditRecordRepository;
        this.inventoryAuditOutboxRepository = inventoryAuditOutboxRepository;
        this.idGenerator = idGenerator;
    }

    public void recordReserveSuccess(InventoryReservation reservation, Instant occurredAt) {
        recordLedgerBatch(reservation, reservation.getItems(), InventoryLedgerType.RESERVE, occurredAt);
        recordAuditAfterCommit(reservation, InventoryAuditActionType.RESERVE, occurredAt);
    }

    public void recordReserveFailed(InventoryReservation reservation, Instant occurredAt) {
        recordAuditAfterCommit(reservation, InventoryAuditActionType.RESERVE_FAILED, occurredAt);
    }

    public void recordReleaseSuccess(InventoryReservation reservation, Instant occurredAt) {
        recordLedgerBatch(reservation, reservation.getItems(), InventoryLedgerType.RELEASE, occurredAt);
        recordAuditAfterCommit(reservation, InventoryAuditActionType.RELEASE, occurredAt);
    }

    public void recordDeductSuccess(InventoryReservation reservation, Instant occurredAt) {
        recordLedgerBatch(reservation, reservation.getItems(), InventoryLedgerType.DEDUCT, occurredAt);
        recordAuditAfterCommit(reservation, InventoryAuditActionType.DEDUCT, occurredAt);
    }

    private void recordLedgerBatch(
            InventoryReservation reservation,
            List<InventoryReservationItem> items,
            InventoryLedgerType ledgerType,
            Instant occurredAt) {
        for (InventoryReservationItem item : items) {
            inventoryAuditRecordRepository.insertLedger(InventoryLedger.create(
                    idGenerator.nextId(LEDGER_ID_BIZ_TAG),
                    reservation.getOrderNo(),
                    reservation.getReservationNo(),
                    item.getSkuId(),
                    reservation.getWarehouseCode(),
                    ledgerType,
                    item.getQuantity(),
                    occurredAt));
        }
    }

    private void recordAuditAfterCommit(
            InventoryReservation reservation, InventoryAuditActionType actionType, Instant occurredAt) {
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

    private void saveAuditSafely(
            InventoryReservation reservation, InventoryAuditActionType actionType, Instant occurredAt) {
        try {
            inventoryAuditRecordRepository.insertLog(InventoryAuditLog.create(
                    idGenerator.nextId(AUDIT_LOG_ID_BIZ_TAG),
                    reservation.getOrderNo(),
                    reservation.getReservationNo(),
                    actionType,
                    InventoryAuditOperatorType.SYSTEM,
                    InventoryAuditLog.OPERATOR_ID_SYSTEM,
                    occurredAt));
            Metrics.counter("bacon.inventory.audit.write.success.total", "actionType", actionType.value())
                    .increment();
        } catch (RuntimeException ex) {
            Metrics.counter("bacon.inventory.audit.write.fail.total", "actionType", actionType.value())
                    .increment();
            saveAuditOutboxSafely(reservation, actionType, occurredAt, ex);
            log.error(
                    "ALERT inventory audit write failed, orderNo={}, reservationNo={}, actionType={}",
                    OrderNoCodec.toValue(reservation.getOrderNo()),
                    ReservationNoCodec.toValue(reservation.getReservationNo()),
                    actionType.value(),
                    ex);
        }
    }

    private void saveAuditOutboxSafely(
            InventoryReservation reservation,
            InventoryAuditActionType actionType,
            Instant occurredAt,
            RuntimeException ex) {
        try {
            inventoryAuditOutboxRepository.insert(InventoryAuditOutbox.create(
                    OutboxId.of(idGenerator.nextId(AUDIT_OUTBOX_ID_BIZ_TAG)),
                    null,
                    reservation.getOrderNo(),
                    reservation.getReservationNo(),
                    actionType,
                    InventoryAuditOperatorType.SYSTEM,
                    String.valueOf(InventoryAuditLog.OPERATOR_ID_SYSTEM),
                    occurredAt,
                    truncateErrorMessage(ex.getMessage()),
                    InventoryAuditOutboxStatus.NEW,
                    0,
                    Instant.now(),
                    null,
                    null,
                    null,
                    null,
                    Instant.now(),
                    Instant.now()));
            Metrics.counter("bacon.inventory.audit.outbox.persist.success.total", "actionType", actionType.value())
                    .increment();
        } catch (RuntimeException outboxEx) {
            Metrics.counter("bacon.inventory.audit.outbox.persist.fail.total", "actionType", actionType.value())
                    .increment();
            log.error(
                    "ALERT inventory audit outbox persist failed, orderNo={}, reservationNo={}, actionType={}",
                    OrderNoCodec.toValue(reservation.getOrderNo()),
                    ReservationNoCodec.toValue(reservation.getReservationNo()),
                    actionType.value(),
                    outboxEx);
        }
    }

    private String truncateErrorMessage(String message) {
        if (message == null || message.isBlank()) {
            return "UNKNOWN";
        }
        return message.length() <= 512 ? message : message.substring(0, 512);
    }
}
