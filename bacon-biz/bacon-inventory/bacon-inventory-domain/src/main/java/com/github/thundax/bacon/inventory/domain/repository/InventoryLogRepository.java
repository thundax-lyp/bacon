package com.github.thundax.bacon.inventory.domain.repository;

import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditDeadLetter;
import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditOutbox;
import com.github.thundax.bacon.inventory.domain.entity.InventoryLedger;
import java.time.Instant;
import java.util.List;

public interface InventoryLogRepository {

    void saveLedger(InventoryLedger ledger);

    List<InventoryLedger> findLedgers(Long tenantId, String orderNo);

    void saveAuditLog(InventoryAuditLog auditLog);

    List<InventoryAuditLog> findAuditLogs(Long tenantId, String orderNo);

    default void saveAuditOutbox(InventoryAuditOutbox outbox) {
    }

    default List<InventoryAuditOutbox> findRetryableAuditOutbox(Instant now, int limit) {
        return List.of();
    }

    default void updateAuditOutboxForRetry(Long outboxId, int retryCount, Instant nextRetryAt, String errorMessage,
                                           Instant updatedAt) {
    }

    default void markAuditOutboxDead(Long outboxId, int retryCount, String deadReason, Instant updatedAt) {
    }

    default void deleteAuditOutbox(Long outboxId) {
    }

    default void saveAuditDeadLetter(InventoryAuditDeadLetter deadLetter) {
    }
}
