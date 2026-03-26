package com.github.thundax.bacon.inventory.application.service;

import com.github.thundax.bacon.inventory.application.audit.*;
import com.github.thundax.bacon.inventory.application.assembler.*;
import com.github.thundax.bacon.inventory.application.command.*;
import com.github.thundax.bacon.inventory.application.query.*;
import com.github.thundax.bacon.inventory.application.support.*;
import com.github.thundax.bacon.inventory.api.dto.InventoryAuditReplayTaskCreateDTO;
import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditDeadLetter;
import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditReplayTask;
import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditReplayTaskItem;
import com.github.thundax.bacon.inventory.domain.entity.InventoryLedger;
import com.github.thundax.bacon.inventory.domain.repository.InventoryLogRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InventoryAuditReplayTaskServiceTest {

    @Test
    void shouldCreateAndProcessReplayTask() {
        TestLogRepository repository = new TestLogRepository();
        repository.saveAuditDeadLetter(new InventoryAuditDeadLetter(101L, 201L, 3001L, "ORDER-1", "RSV-1",
                InventoryAuditLog.ACTION_RESERVE, InventoryAuditLog.OPERATOR_TYPE_SYSTEM,
                InventoryAuditLog.OPERATOR_ID_SYSTEM, Instant.parse("2026-03-26T00:00:00Z"), 1, "FAIL",
                "MAX_RETRIES_EXCEEDED", Instant.parse("2026-03-26T00:01:00Z")));

        InventoryAuditReplayTaskService taskService = new InventoryAuditReplayTaskService(repository);
        InventoryAuditCompensationService compensationService = new InventoryAuditCompensationService(repository,
                new InventoryAuditReplayTransactionFacade(repository, new InventoryTransactionExecutor()));

        var created = taskService.createReplayTask(new InventoryAuditReplayTaskCreateDTO(3001L, 9001L,
                "TASK-BATCH", List.of(101L)));
        assertNotNull(created.getTaskId());

        String owner = "test-owner";
        List<InventoryAuditReplayTask> claimed = repository.claimRunnableAuditReplayTasks(Instant.now(), 1, owner,
                Instant.now().plusSeconds(60));
        taskService.processClaimedTask(claimed.get(0), compensationService, owner, 10, 60);

        InventoryAuditReplayTask finished = repository.findAuditReplayTaskById(created.getTaskId()).orElseThrow();
        assertEquals(InventoryAuditReplayTask.STATUS_SUCCEEDED, finished.getStatus());
        assertEquals(1, finished.getProcessedCount());
        assertEquals(1, finished.getSuccessCount());
        assertEquals(0, finished.getFailedCount());
    }

    private static final class TestLogRepository implements InventoryLogRepository {

        private final AtomicLong taskIdGenerator = new AtomicLong(1000L);
        private final AtomicLong taskItemIdGenerator = new AtomicLong(2000L);
        private final Map<Long, InventoryAuditDeadLetter> deadLetters = new ConcurrentHashMap<>();
        private final Map<Long, InventoryAuditReplayTask> tasks = new ConcurrentHashMap<>();
        private final Map<Long, List<InventoryAuditReplayTaskItem>> taskItems = new ConcurrentHashMap<>();
        private final List<InventoryAuditLog> auditLogs = new ArrayList<>();

        @Override
        public void saveAuditLog(InventoryAuditLog auditLog) {
            auditLogs.add(auditLog);
        }

        @Override
        public void saveLedger(InventoryLedger ledger) {
        }

        @Override
        public List<InventoryLedger> findLedgers(Long tenantId, String orderNo) {
            return List.of();
        }

        @Override
        public List<InventoryAuditLog> findAuditLogs(Long tenantId, String orderNo) {
            return List.copyOf(auditLogs);
        }

        @Override
        public void saveAuditDeadLetter(InventoryAuditDeadLetter deadLetter) {
            deadLetters.put(deadLetter.getId(), deadLetter);
        }

        @Override
        public Optional<InventoryAuditDeadLetter> findAuditDeadLetterById(Long id) {
            return Optional.ofNullable(deadLetters.get(id));
        }

        @Override
        public boolean claimAuditDeadLetterForReplay(Long id, Long tenantId, String replayKey,
                                                     String operatorType, Long operatorId, Instant replayAt) {
            InventoryAuditDeadLetter deadLetter = deadLetters.get(id);
            if (deadLetter == null || !tenantId.equals(deadLetter.getTenantId())) {
                return false;
            }
            if (!InventoryAuditDeadLetter.REPLAY_STATUS_PENDING.equals(deadLetter.getReplayStatus())
                    && !InventoryAuditDeadLetter.REPLAY_STATUS_FAILED.equals(deadLetter.getReplayStatus())) {
                return false;
            }
            deadLetter.setReplayStatus(InventoryAuditDeadLetter.REPLAY_STATUS_RUNNING);
            deadLetter.setReplayKey(replayKey);
            deadLetter.setReplayOperatorType(operatorType);
            deadLetter.setReplayOperatorId(operatorId);
            deadLetter.setLastReplayAt(replayAt);
            deadLetter.setLastReplayResult("RUNNING");
            deadLetter.setLastReplayError(null);
            return true;
        }

        @Override
        public void markAuditDeadLetterReplaySuccess(Long id, String replayKey, String operatorType, Long operatorId,
                                                     Instant replayAt) {
            InventoryAuditDeadLetter deadLetter = deadLetters.get(id);
            deadLetter.setReplayStatus(InventoryAuditDeadLetter.REPLAY_STATUS_SUCCEEDED);
            deadLetter.setReplayCount((deadLetter.getReplayCount() == null ? 0 : deadLetter.getReplayCount()) + 1);
            deadLetter.setReplayKey(replayKey);
            deadLetter.setReplayOperatorType(operatorType);
            deadLetter.setReplayOperatorId(operatorId);
            deadLetter.setLastReplayAt(replayAt);
            deadLetter.setLastReplayResult("SUCCEEDED");
            deadLetter.setLastReplayError(null);
        }

        @Override
        public void markAuditDeadLetterReplayFailed(Long id, String replayKey, String operatorType, Long operatorId,
                                                    String replayError, Instant replayAt) {
            InventoryAuditDeadLetter deadLetter = deadLetters.get(id);
            deadLetter.setReplayStatus(InventoryAuditDeadLetter.REPLAY_STATUS_FAILED);
            deadLetter.setReplayCount((deadLetter.getReplayCount() == null ? 0 : deadLetter.getReplayCount()) + 1);
            deadLetter.setReplayKey(replayKey);
            deadLetter.setReplayOperatorType(operatorType);
            deadLetter.setReplayOperatorId(operatorId);
            deadLetter.setLastReplayAt(replayAt);
            deadLetter.setLastReplayResult("FAILED");
            deadLetter.setLastReplayError(replayError);
        }

        @Override
        public InventoryAuditReplayTask saveAuditReplayTask(InventoryAuditReplayTask task) {
            if (task.getId() == null) {
                task.setId(taskIdGenerator.getAndIncrement());
            }
            tasks.put(task.getId(), task);
            return task;
        }

        @Override
        public void batchSaveAuditReplayTaskItems(Long taskId, Long tenantId, List<Long> deadLetterIds, Instant createdAt) {
            List<InventoryAuditReplayTaskItem> items = taskItems.computeIfAbsent(taskId, key -> new ArrayList<>());
            for (Long deadLetterId : deadLetterIds) {
                items.add(new InventoryAuditReplayTaskItem(taskItemIdGenerator.getAndIncrement(), taskId, tenantId,
                        deadLetterId, InventoryAuditReplayTaskItem.STATUS_PENDING, null, null, null, null, null,
                        createdAt));
            }
        }

        @Override
        public Optional<InventoryAuditReplayTask> findAuditReplayTaskById(Long taskId) {
            return Optional.ofNullable(tasks.get(taskId));
        }

        @Override
        public List<InventoryAuditReplayTask> claimRunnableAuditReplayTasks(Instant now, int limit,
                                                                            String processingOwner, Instant leaseUntil) {
            return tasks.values().stream()
                    .filter(task -> InventoryAuditReplayTask.STATUS_PENDING.equals(task.getStatus())
                            || InventoryAuditReplayTask.STATUS_RUNNING.equals(task.getStatus()))
                    .filter(task -> task.getLeaseUntil() == null || !task.getLeaseUntil().isAfter(now))
                    .sorted(Comparator.comparing(InventoryAuditReplayTask::getId))
                    .limit(limit)
                    .peek(task -> {
                        task.setStatus(InventoryAuditReplayTask.STATUS_RUNNING);
                        task.setProcessingOwner(processingOwner);
                        task.setLeaseUntil(leaseUntil);
                        if (task.getStartedAt() == null) {
                            task.setStartedAt(now);
                        }
                        task.setUpdatedAt(now);
                    })
                    .toList();
        }

        @Override
        public void renewAuditReplayTaskLease(Long taskId, String processingOwner, Instant leaseUntil, Instant updatedAt) {
            InventoryAuditReplayTask task = tasks.get(taskId);
            if (task != null && processingOwner.equals(task.getProcessingOwner())) {
                task.setLeaseUntil(leaseUntil);
                task.setUpdatedAt(updatedAt);
            }
        }

        @Override
        public List<InventoryAuditReplayTaskItem> findPendingAuditReplayTaskItems(Long taskId, int limit) {
            return taskItems.getOrDefault(taskId, List.of()).stream()
                    .filter(item -> InventoryAuditReplayTaskItem.STATUS_PENDING.equals(item.getItemStatus()))
                    .sorted(Comparator.comparing(InventoryAuditReplayTaskItem::getId))
                    .limit(limit)
                    .toList();
        }

        @Override
        public void markAuditReplayTaskItemResult(Long itemId, String itemStatus, String replayStatus,
                                                  String replayKey, String resultMessage, Instant startedAt,
                                                  Instant finishedAt) {
            taskItems.values().forEach(items -> items.stream()
                    .filter(item -> item.getId().equals(itemId))
                    .findFirst()
                    .ifPresent(item -> {
                        item.setItemStatus(itemStatus);
                        item.setReplayStatus(replayStatus);
                        item.setReplayKey(replayKey);
                        item.setResultMessage(resultMessage);
                        item.setStartedAt(startedAt);
                        item.setFinishedAt(finishedAt);
                        item.setUpdatedAt(finishedAt);
                    }));
        }

        @Override
        public void incrementAuditReplayTaskProgress(Long taskId, String processingOwner, int processedDelta,
                                                     int successDelta, int failedDelta, Instant updatedAt) {
            InventoryAuditReplayTask task = tasks.get(taskId);
            if (task == null || !processingOwner.equals(task.getProcessingOwner())) {
                return;
            }
            task.setProcessedCount(task.getProcessedCount() + processedDelta);
            task.setSuccessCount(task.getSuccessCount() + successDelta);
            task.setFailedCount(task.getFailedCount() + failedDelta);
            task.setUpdatedAt(updatedAt);
        }

        @Override
        public void finishAuditReplayTask(Long taskId, String processingOwner, String status, String lastError,
                                          Instant finishedAt) {
            InventoryAuditReplayTask task = tasks.get(taskId);
            if (task == null || !processingOwner.equals(task.getProcessingOwner())) {
                return;
            }
            task.setStatus(status);
            task.setLastError(lastError);
            task.setProcessingOwner(null);
            task.setLeaseUntil(null);
            task.setFinishedAt(finishedAt);
            task.setUpdatedAt(finishedAt);
        }
    }
}
