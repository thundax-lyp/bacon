package com.github.thundax.bacon.inventory.domain.repository;

import com.github.thundax.bacon.common.id.domain.OperatorId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditReplayTask;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditReplayTaskItem;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayStatus;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayTaskItemStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.DeadLetterId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.TaskId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface InventoryAuditReplayTaskRepository {

    default InventoryAuditReplayTask saveAuditReplayTask(InventoryAuditReplayTask task) {
        return task;
    }

    default void batchSaveAuditReplayTaskItems(
            TaskId taskId, TenantId tenantId, List<DeadLetterId> deadLetterIds, Instant createdAt) {}

    default Optional<InventoryAuditReplayTask> findAuditReplayTaskById(TaskId taskId) {
        return Optional.empty();
    }

    default List<InventoryAuditReplayTask> claimRunnableAuditReplayTasks(
            Instant now, int limit, String processingOwner, Instant leaseUntil) {
        return List.of();
    }

    default void renewAuditReplayTaskLease(
            TaskId taskId, String processingOwner, Instant leaseUntil, Instant updatedAt) {}

    default List<InventoryAuditReplayTaskItem> findPendingAuditReplayTaskItems(TaskId taskId, int limit) {
        return List.of();
    }

    default void markAuditReplayTaskItemResult(
            Long itemId,
            InventoryAuditReplayTaskItemStatus itemStatus,
            InventoryAuditReplayStatus replayStatus,
            String replayKey,
            String resultMessage,
            Instant startedAt,
            Instant finishedAt) {}

    default void incrementAuditReplayTaskProgress(
            TaskId taskId,
            String processingOwner,
            int processedDelta,
            int successDelta,
            int failedDelta,
            Instant updatedAt) {}

    default void finishAuditReplayTask(
            TaskId taskId, String processingOwner, String status, String lastError, Instant finishedAt) {}

    default boolean pauseAuditReplayTask(TaskId taskId, TenantId tenantId, OperatorId operatorId, Instant pausedAt) {
        return false;
    }

    default boolean resumeAuditReplayTask(TaskId taskId, TenantId tenantId, OperatorId operatorId, Instant updatedAt) {
        return false;
    }
}
