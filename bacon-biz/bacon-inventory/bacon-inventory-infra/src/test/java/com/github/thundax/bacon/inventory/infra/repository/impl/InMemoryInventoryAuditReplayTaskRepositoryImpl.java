package com.github.thundax.bacon.inventory.infra.repository.impl;

import com.github.thundax.bacon.common.id.domain.OperatorId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditReplayTask;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditReplayTaskItem;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayStatus;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayTaskItemStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.DeadLetterId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.TaskId;
import com.github.thundax.bacon.inventory.domain.repository.InventoryAuditReplayTaskRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Primary
@Profile("test")
public class InMemoryInventoryAuditReplayTaskRepositoryImpl implements InventoryAuditReplayTaskRepository {

    private final InMemoryInventoryRepositorySupport support;

    public InMemoryInventoryAuditReplayTaskRepositoryImpl(InMemoryInventoryRepositorySupport support) {
        this.support = support;
    }

    @Override
    public InventoryAuditReplayTask saveAuditReplayTask(InventoryAuditReplayTask task) {
        return support.saveAuditReplayTask(task);
    }

    @Override
    public void batchSaveAuditReplayTaskItems(
            TaskId taskId, TenantId tenantId, List<DeadLetterId> deadLetterIds, Instant createdAt) {
        support.batchSaveAuditReplayTaskItems(taskId, tenantId, deadLetterIds, createdAt);
    }

    @Override
    public Optional<InventoryAuditReplayTask> findAuditReplayTaskById(TaskId taskId) {
        return support.findAuditReplayTaskById(taskId);
    }

    @Override
    public List<InventoryAuditReplayTask> claimRunnableAuditReplayTasks(
            Instant now, int limit, String processingOwner, Instant leaseUntil) {
        return support.claimRunnableAuditReplayTasks(now, limit, processingOwner, leaseUntil);
    }

    @Override
    public void renewAuditReplayTaskLease(
            TaskId taskId, String processingOwner, Instant leaseUntil, Instant updatedAt) {
        support.renewAuditReplayTaskLease(taskId, processingOwner, leaseUntil, updatedAt);
    }

    @Override
    public List<InventoryAuditReplayTaskItem> findPendingAuditReplayTaskItems(TaskId taskId, int limit) {
        return support.findPendingAuditReplayTaskItems(taskId, limit);
    }

    @Override
    public void markAuditReplayTaskItemResult(
            Long itemId,
            InventoryAuditReplayTaskItemStatus itemStatus,
            InventoryAuditReplayStatus replayStatus,
            String replayKey,
            String resultMessage,
            Instant startedAt,
            Instant finishedAt) {
        support.markAuditReplayTaskItemResult(
                itemId, itemStatus, replayStatus, replayKey, resultMessage, startedAt, finishedAt);
    }

    @Override
    public void incrementAuditReplayTaskProgress(
            TaskId taskId,
            String processingOwner,
            int processedDelta,
            int successDelta,
            int failedDelta,
            Instant updatedAt) {
        support.incrementAuditReplayTaskProgress(
                taskId, processingOwner, processedDelta, successDelta, failedDelta, updatedAt);
    }

    @Override
    public void finishAuditReplayTask(
            TaskId taskId, String processingOwner, String status, String lastError, Instant finishedAt) {
        support.finishAuditReplayTask(taskId, processingOwner, status, lastError, finishedAt);
    }

    @Override
    public boolean pauseAuditReplayTask(TaskId taskId, TenantId tenantId, OperatorId operatorId, Instant pausedAt) {
        return support.pauseAuditReplayTask(taskId, tenantId, operatorId, pausedAt);
    }

    @Override
    public boolean resumeAuditReplayTask(TaskId taskId, TenantId tenantId, OperatorId operatorId, Instant updatedAt) {
        return support.resumeAuditReplayTask(taskId, tenantId, operatorId, updatedAt);
    }
}
