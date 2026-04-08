package com.github.thundax.bacon.inventory.domain.repository;

import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditReplayTask;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditReplayTaskItem;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayStatus;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayTaskItemStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface InventoryAuditReplayTaskRepository {

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

    default void markAuditReplayTaskItemResult(Long itemId, InventoryAuditReplayTaskItemStatus itemStatus,
                                               InventoryAuditReplayStatus replayStatus,
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
