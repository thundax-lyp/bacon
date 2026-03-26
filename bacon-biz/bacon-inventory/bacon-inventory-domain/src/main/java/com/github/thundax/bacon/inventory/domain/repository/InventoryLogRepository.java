package com.github.thundax.bacon.inventory.domain.repository;

import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditDeadLetter;
import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditOutbox;
import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditReplayTask;
import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditReplayTaskItem;
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

    default InventoryAuditReplayTask saveAuditReplayTask(InventoryAuditReplayTask task) {
        return task;
    }

    default void batchSaveAuditReplayTaskItems(Long taskId, Long tenantId, List<Long> deadLetterIds, Instant createdAt) {
    }

    default Optional<InventoryAuditReplayTask> findAuditReplayTaskById(Long taskId) {
        return Optional.empty();
    }

    default List<InventoryAuditReplayTask> claimRunnableAuditReplayTasks(Instant now, int limit,
                                                                         String processingOwner, Instant leaseUntil) {
        return List.of();
    }

    default void renewAuditReplayTaskLease(Long taskId, String processingOwner, Instant leaseUntil, Instant updatedAt) {
    }

    default List<InventoryAuditReplayTaskItem> findPendingAuditReplayTaskItems(Long taskId, int limit) {
        return List.of();
    }

    default void markAuditReplayTaskItemResult(Long itemId, String itemStatus, String replayStatus,
                                               String replayKey, String resultMessage, Instant startedAt,
                                               Instant finishedAt) {
    }

    default void incrementAuditReplayTaskProgress(Long taskId, String processingOwner, int processedDelta,
                                                  int successDelta, int failedDelta, Instant updatedAt) {
    }

    default void finishAuditReplayTask(Long taskId, String processingOwner, String status,
                                       String lastError, Instant finishedAt) {
    }

    default boolean pauseAuditReplayTask(Long taskId, Long tenantId, Long operatorId, Instant pausedAt) {
        return false;
    }

    default boolean resumeAuditReplayTask(Long taskId, Long tenantId, Long operatorId, Instant updatedAt) {
        return false;
    }
}
