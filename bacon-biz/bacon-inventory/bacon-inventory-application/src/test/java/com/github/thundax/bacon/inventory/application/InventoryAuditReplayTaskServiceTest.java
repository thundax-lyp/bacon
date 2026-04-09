package com.github.thundax.bacon.inventory.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.id.domain.OperatorId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.api.dto.InventoryAuditReplayTaskCreateDTO;
import com.github.thundax.bacon.inventory.application.audit.InventoryAuditCompensationApplicationService;
import com.github.thundax.bacon.inventory.application.audit.InventoryAuditReplayTaskApplicationService;
import com.github.thundax.bacon.inventory.application.audit.InventoryAuditReplayTransactionExecutor;
import com.github.thundax.bacon.inventory.application.codec.OutboxIdCodec;
import com.github.thundax.bacon.inventory.application.support.InventoryTransactionExecutor;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditDeadLetter;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditReplayTask;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditReplayTaskItem;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryLedger;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditActionType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditOperatorType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayStatus;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayTaskItemStatus;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayTaskStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.DeadLetterId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.EventCode;
import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservationNo;
import com.github.thundax.bacon.inventory.domain.model.valueobject.TaskId;
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

class InventoryAuditReplayTaskApplicationServiceTest {

    @Test
    void shouldCreateAndProcessReplayTask() {
        TestLogRepository repository = new TestLogRepository();
        repository.saveAuditDeadLetter(InventoryAuditDeadLetter.create(
                com.github.thundax.bacon.inventory.domain.model.valueobject.OutboxId.of(101L),
                EventCode.of("EVT20260326000000-000101"),
                TenantId.of(3001L),
                OrderNo.of("ORDER-1"),
                ReservationNo.of("RSV-1"),
                InventoryAuditActionType.RESERVE,
                InventoryAuditOperatorType.SYSTEM,
                String.valueOf(InventoryAuditLog.OPERATOR_ID_SYSTEM),
                Instant.parse("2026-03-26T00:00:00Z"),
                1,
                "FAIL",
                "MAX_RETRIES_EXCEEDED",
                Instant.parse("2026-03-26T00:01:00Z"),
                InventoryAuditReplayStatus.PENDING,
                0,
                null,
                null,
                null,
                null,
                null,
                null));

        InventoryAuditReplayTaskApplicationService taskService =
                new InventoryAuditReplayTaskApplicationService(repository);
        InventoryAuditCompensationApplicationService compensationService =
                new InventoryAuditCompensationApplicationService(
                        repository,
                        new InventoryAuditReplayTransactionExecutor(
                                repository, repository, new InventoryTransactionExecutor()));

        var created = taskService.createReplayTask(
                new InventoryAuditReplayTaskCreateDTO(3001L, 9001L, "TASK-BATCH", List.of(101L)));
        assertNotNull(created.getTaskId());

        String owner = "test-owner";
        List<InventoryAuditReplayTask> claimed = repository.claimRunnableAuditReplayTasks(
                Instant.now(), 1, owner, Instant.now().plusSeconds(60));
        taskService.processClaimedTask(claimed.get(0), compensationService, owner, 10, 60);

        InventoryAuditReplayTask finished = repository
                .findAuditReplayTaskById(TaskId.of(created.getTaskId()))
                .orElseThrow();
        assertEquals(InventoryAuditReplayTaskStatus.SUCCEEDED, finished.getStatus());
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
        public void saveLedger(InventoryLedger ledger) {}

        @Override
        public List<InventoryLedger> findLedgers(TenantId tenantId, OrderNo orderNo) {
            return List.of();
        }

        @Override
        public List<InventoryAuditLog> findAuditLogs(TenantId tenantId, OrderNo orderNo) {
            return List.copyOf(auditLogs);
        }

        @Override
        public void saveAuditDeadLetter(InventoryAuditDeadLetter deadLetter) {
            deadLetters.put(OutboxIdCodec.toValue(deadLetter.getOutboxId()), deadLetter);
        }

        @Override
        public Optional<InventoryAuditDeadLetter> findAuditDeadLetterById(DeadLetterId id) {
            return Optional.ofNullable(deadLetters.get(id.value()));
        }

        @Override
        public boolean claimAuditDeadLetterForReplay(
                DeadLetterId id,
                TenantId tenantId,
                String replayKey,
                InventoryAuditOperatorType operatorType,
                OperatorId operatorId,
                Instant replayAt) {
            InventoryAuditDeadLetter deadLetter = deadLetters.get(id.value());
            if (deadLetter == null || !tenantId.equals(deadLetter.getTenantId())) {
                return false;
            }
            if (!InventoryAuditReplayStatus.PENDING.equals(deadLetter.getReplayStatus())
                    && !InventoryAuditReplayStatus.FAILED.equals(deadLetter.getReplayStatus())) {
                return false;
            }
            deadLetter.markReplayRunning(
                    replayKey, operatorType, operatorId == null ? null : operatorId.value(), replayAt);
            return true;
        }

        @Override
        public void markAuditDeadLetterReplaySuccess(
                DeadLetterId id,
                String replayKey,
                InventoryAuditOperatorType operatorType,
                OperatorId operatorId,
                Instant replayAt) {
            InventoryAuditDeadLetter deadLetter = deadLetters.get(id.value());
            deadLetter.markReplaySucceeded(
                    replayKey, operatorType, operatorId == null ? null : operatorId.value(), replayAt);
        }

        @Override
        public void markAuditDeadLetterReplayFailed(
                DeadLetterId id,
                String replayKey,
                InventoryAuditOperatorType operatorType,
                OperatorId operatorId,
                String replayError,
                Instant replayAt) {
            InventoryAuditDeadLetter deadLetter = deadLetters.get(id.value());
            deadLetter.markReplayFailed(
                    replayKey, operatorType, operatorId == null ? null : operatorId.value(), replayError, replayAt);
        }

        @Override
        public InventoryAuditReplayTask saveAuditReplayTask(InventoryAuditReplayTask task) {
            if (task.getId() == null) {
                task.setId(TaskId.of(taskIdGenerator.getAndIncrement()));
            }
            tasks.put(task.getIdValue(), task);
            return task;
        }

        @Override
        public void batchSaveAuditReplayTaskItems(
                TaskId taskId, TenantId tenantId, List<DeadLetterId> deadLetterIds, Instant createdAt) {
            List<InventoryAuditReplayTaskItem> items =
                    taskItems.computeIfAbsent(taskId == null ? null : taskId.value(), key -> new ArrayList<>());
            for (DeadLetterId deadLetterId : deadLetterIds) {
                items.add(new InventoryAuditReplayTaskItem(
                        taskItemIdGenerator.getAndIncrement(),
                        taskId,
                        tenantId,
                        deadLetterId,
                        InventoryAuditReplayTaskItemStatus.PENDING,
                        null,
                        null,
                        null,
                        null,
                        null,
                        createdAt));
            }
        }

        @Override
        public Optional<InventoryAuditReplayTask> findAuditReplayTaskById(TaskId taskId) {
            return Optional.ofNullable(tasks.get(taskId == null ? null : taskId.value()));
        }

        @Override
        public List<InventoryAuditReplayTask> claimRunnableAuditReplayTasks(
                Instant now, int limit, String processingOwner, Instant leaseUntil) {
            return tasks.values().stream()
                    .filter(task -> InventoryAuditReplayTaskStatus.PENDING.equals(task.getStatus())
                            || InventoryAuditReplayTaskStatus.RUNNING.equals(task.getStatus()))
                    .filter(task -> task.getLeaseUntil() == null
                            || !task.getLeaseUntil().isAfter(now))
                    .sorted(Comparator.comparing(InventoryAuditReplayTask::getIdValue))
                    .limit(limit)
                    .peek(task -> {
                        task.setStatus(InventoryAuditReplayTaskStatus.RUNNING);
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
        public void renewAuditReplayTaskLease(
                TaskId taskId, String processingOwner, Instant leaseUntil, Instant updatedAt) {
            InventoryAuditReplayTask task = tasks.get(taskId == null ? null : taskId.value());
            if (task != null && processingOwner.equals(task.getProcessingOwner())) {
                task.setLeaseUntil(leaseUntil);
                task.setUpdatedAt(updatedAt);
            }
        }

        @Override
        public List<InventoryAuditReplayTaskItem> findPendingAuditReplayTaskItems(TaskId taskId, int limit) {
            return taskItems.getOrDefault(taskId == null ? null : taskId.value(), List.of()).stream()
                    .filter(item -> InventoryAuditReplayTaskItemStatus.PENDING.equals(item.getItemStatus()))
                    .sorted(Comparator.comparing(InventoryAuditReplayTaskItem::getId))
                    .limit(limit)
                    .toList();
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
        public void incrementAuditReplayTaskProgress(
                TaskId taskId,
                String processingOwner,
                int processedDelta,
                int successDelta,
                int failedDelta,
                Instant updatedAt) {
            InventoryAuditReplayTask task = tasks.get(taskId == null ? null : taskId.value());
            if (task == null || !processingOwner.equals(task.getProcessingOwner())) {
                return;
            }
            task.setProcessedCount(task.getProcessedCount() + processedDelta);
            task.setSuccessCount(task.getSuccessCount() + successDelta);
            task.setFailedCount(task.getFailedCount() + failedDelta);
            task.setUpdatedAt(updatedAt);
        }

        @Override
        public void finishAuditReplayTask(
                TaskId taskId, String processingOwner, String status, String lastError, Instant finishedAt) {
            InventoryAuditReplayTask task = tasks.get(taskId == null ? null : taskId.value());
            if (task == null || !processingOwner.equals(task.getProcessingOwner())) {
                return;
            }
            task.setStatus(InventoryAuditReplayTaskStatus.from(status));
            task.setLastError(lastError);
            task.setProcessingOwner(null);
            task.setLeaseUntil(null);
            task.setFinishedAt(finishedAt);
            task.setUpdatedAt(finishedAt);
        }
    }
}
