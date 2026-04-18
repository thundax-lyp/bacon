package com.github.thundax.bacon.inventory.infra.repository.impl;

import com.github.thundax.bacon.common.id.domain.OperatorId;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditReplayTask;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditReplayTaskItem;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayStatus;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayTaskItemStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.TaskId;
import com.github.thundax.bacon.inventory.domain.repository.InventoryAuditReplayTaskRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
public class InventoryAuditReplayTaskRepositoryImpl implements InventoryAuditReplayTaskRepository {

    private final InventoryRepositorySupport support;

    public InventoryAuditReplayTaskRepositoryImpl(InventoryRepositorySupport support) {
        this.support = support;
    }

    @Override
    public InventoryAuditReplayTask insert(InventoryAuditReplayTask task) {
        return support.insert(task);
    }

    @Override
    public void insertItems(List<InventoryAuditReplayTaskItem> items) {
        support.insertItems(items);
    }

    @Override
    public Optional<InventoryAuditReplayTask> findById(TaskId taskId) {
        return support.findById(taskId);
    }

    @Override
    public Long findTenantIdById(TaskId taskId) {
        return support.findTenantIdById(taskId);
    }

    @Override
    public List<InventoryAuditReplayTask> claim(
            Instant now, int limit, String processingOwner, Instant leaseUntil) {
        return support.claim(now, limit, processingOwner, leaseUntil);
    }

    @Override
    public void renew(
            TaskId taskId, String processingOwner, Instant leaseUntil, Instant updatedAt) {
        support.renew(taskId, processingOwner, leaseUntil, updatedAt);
    }

    @Override
    public List<InventoryAuditReplayTaskItem> listPendingItems(TaskId taskId, int limit) {
        return support.listPendingItems(taskId, limit);
    }

    @Override
    public void markItemResult(
            Long itemId,
            InventoryAuditReplayTaskItemStatus itemStatus,
            InventoryAuditReplayStatus replayStatus,
            String replayKey,
            String resultMessage,
            Instant startedAt,
            Instant finishedAt) {
        support.markItemResult(
                itemId, itemStatus, replayStatus, replayKey, resultMessage, startedAt, finishedAt);
    }

    @Override
    public void updateProgress(
            TaskId taskId,
            String processingOwner,
            int processedDelta,
            int successDelta,
            int failedDelta,
            Instant updatedAt) {
        support.updateProgress(
                taskId, processingOwner, processedDelta, successDelta, failedDelta, updatedAt);
    }

    @Override
    public void markFinished(
            TaskId taskId, String processingOwner, String status, String lastError, Instant finishedAt) {
        support.markFinished(taskId, processingOwner, status, lastError, finishedAt);
    }

    @Override
    public boolean pause(TaskId taskId, OperatorId operatorId, Instant pausedAt) {
        return support.pause(taskId, operatorId, pausedAt);
    }

    @Override
    public boolean resume(TaskId taskId, OperatorId operatorId, Instant updatedAt) {
        return support.resume(taskId, operatorId, updatedAt);
    }
}
