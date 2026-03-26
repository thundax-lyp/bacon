package com.github.thundax.bacon.inventory.domain.repository;

import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditDeadLetter;
import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditOutbox;
import com.github.thundax.bacon.inventory.domain.entity.InventoryLedger;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

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

    default List<InventoryAuditOutbox> claimRetryableAuditOutbox(Instant now, int limit,
                                                                  String processingOwner, Instant leaseUntil) {
        return List.of();
    }

    default int releaseExpiredAuditOutboxLease(Instant now) {
        return 0;
    }

    default void updateAuditOutboxForRetry(Long outboxId, int retryCount, Instant nextRetryAt, String errorMessage,
                                           Instant updatedAt) {
    }

    default boolean updateAuditOutboxForRetryClaimed(Long outboxId, String processingOwner, int retryCount,
                                                     Instant nextRetryAt, String errorMessage, Instant updatedAt) {
        return false;
    }

    default void markAuditOutboxDead(Long outboxId, int retryCount, String deadReason, Instant updatedAt) {
    }

    default boolean markAuditOutboxDeadClaimed(Long outboxId, String processingOwner, int retryCount,
                                               String deadReason, Instant updatedAt) {
        return false;
    }

    default void deleteAuditOutbox(Long outboxId) {
    }

    default boolean deleteAuditOutboxClaimed(Long outboxId, String processingOwner) {
        return false;
    }

    default void saveAuditDeadLetter(InventoryAuditDeadLetter deadLetter) {
    }

    default List<InventoryAuditDeadLetter> pageAuditDeadLetters(Long tenantId, String orderNo,
                                                                 String replayStatus, int pageNo, int pageSize) {
        return List.of();
    }

    default long countAuditDeadLetters(Long tenantId, String orderNo, String replayStatus) {
        return 0L;
    }

    default Optional<InventoryAuditDeadLetter> findAuditDeadLetterById(Long id) {
        return Optional.empty();
    }

    default boolean claimAuditDeadLetterForReplay(Long id, Long tenantId, String replayKey,
                                                  String operatorType, Long operatorId, Instant replayAt) {
        return false;
    }

    default void markAuditDeadLetterReplaySuccess(Long id, String replayKey, String operatorType, Long operatorId,
                                                  Instant replayAt) {
    }

    default void markAuditDeadLetterReplayFailed(Long id, String replayKey, String operatorType, Long operatorId,
                                                 String replayError, Instant replayAt) {
    }
}
