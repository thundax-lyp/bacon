package com.github.thundax.bacon.inventory.infra.repository.impl;

import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditReplayTask;
import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditReplayTaskItem;
import com.github.thundax.bacon.inventory.domain.repository.InventoryAuditReplayTaskRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnBean(InMemoryInventoryRepositorySupport.class)
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
    public void batchSaveAuditReplayTaskItems(Long taskId, Long tenantId, List<Long> deadLetterIds, Instant createdAt) {
        support.batchSaveAuditReplayTaskItems(taskId, tenantId, deadLetterIds, createdAt);
    }

    @Override
    public Optional<InventoryAuditReplayTask> findAuditReplayTaskById(Long taskId) {
        return support.findAuditReplayTaskById(taskId);
    }

    @Override
    public List<InventoryAuditReplayTask> claimRunnableAuditReplayTasks(Instant now, int limit,
                                                                        String processingOwner, Instant leaseUntil) {
        return support.claimRunnableAuditReplayTasks(now, limit, processingOwner, leaseUntil);
    }

    @Override
    public void renewAuditReplayTaskLease(Long taskId, String processingOwner, Instant leaseUntil, Instant updatedAt) {
        support.renewAuditReplayTaskLease(taskId, processingOwner, leaseUntil, updatedAt);
    }

    @Override
    public List<InventoryAuditReplayTaskItem> findPendingAuditReplayTaskItems(Long taskId, int limit) {
        return support.findPendingAuditReplayTaskItems(taskId, limit);
    }

    @Override
    public void markAuditReplayTaskItemResult(Long itemId, String itemStatus, String replayStatus,
                                              String replayKey, String resultMessage, Instant startedAt,
                                              Instant finishedAt) {
        support.markAuditReplayTaskItemResult(itemId, itemStatus, replayStatus, replayKey, resultMessage, startedAt,
                finishedAt);
    }

    @Override
    public void incrementAuditReplayTaskProgress(Long taskId, String processingOwner, int processedDelta,
                                                 int successDelta, int failedDelta, Instant updatedAt) {
        support.incrementAuditReplayTaskProgress(taskId, processingOwner, processedDelta, successDelta, failedDelta,
                updatedAt);
    }

    @Override
    public void finishAuditReplayTask(Long taskId, String processingOwner, String status,
                                      String lastError, Instant finishedAt) {
        support.finishAuditReplayTask(taskId, processingOwner, status, lastError, finishedAt);
    }

    @Override
    public boolean pauseAuditReplayTask(Long taskId, Long tenantId, Long operatorId, Instant pausedAt) {
        return support.pauseAuditReplayTask(taskId, tenantId, operatorId, pausedAt);
    }

    @Override
    public boolean resumeAuditReplayTask(Long taskId, Long tenantId, Long operatorId, Instant updatedAt) {
        return support.resumeAuditReplayTask(taskId, tenantId, operatorId, updatedAt);
    }
}
