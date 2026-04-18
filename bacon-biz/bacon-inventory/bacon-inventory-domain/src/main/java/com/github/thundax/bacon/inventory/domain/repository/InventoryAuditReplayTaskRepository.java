package com.github.thundax.bacon.inventory.domain.repository;

import com.github.thundax.bacon.common.id.domain.OperatorId;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditReplayTask;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditReplayTaskItem;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayStatus;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayTaskItemStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.TaskId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface InventoryAuditReplayTaskRepository {

    default InventoryAuditReplayTask insert(InventoryAuditReplayTask task) {
        return task;
    }

    default void insertItems(List<InventoryAuditReplayTaskItem> items) {}

    default Optional<InventoryAuditReplayTask> findById(TaskId taskId) {
        return Optional.empty();
    }

    default Long findTenantIdById(TaskId taskId) {
        return null;
    }

    default List<InventoryAuditReplayTask> claim(
            Instant now, int limit, String processingOwner, Instant leaseUntil) {
        return List.of();
    }

    default void renew(
            TaskId taskId, String processingOwner, Instant leaseUntil, Instant updatedAt) {}

    default List<InventoryAuditReplayTaskItem> listPendingItems(TaskId taskId, int limit) {
        return List.of();
    }

    default void markItemResult(
            Long itemId,
            InventoryAuditReplayTaskItemStatus itemStatus,
            InventoryAuditReplayStatus replayStatus,
            String replayKey,
            String resultMessage,
            Instant startedAt,
            Instant finishedAt) {}

    default void updateProgress(
            TaskId taskId,
            String processingOwner,
            int processedDelta,
            int successDelta,
            int failedDelta,
            Instant updatedAt) {}

    default void markFinished(
            TaskId taskId, String processingOwner, String status, String lastError, Instant finishedAt) {}

    default boolean pause(TaskId taskId, OperatorId operatorId, Instant pausedAt) {
        return false;
    }

    default boolean resume(TaskId taskId, OperatorId operatorId, Instant updatedAt) {
        return false;
    }
}
