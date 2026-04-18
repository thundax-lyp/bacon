package com.github.thundax.bacon.inventory.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.domain.OperatorId;
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
import org.junit.jupiter.api.Test;

class InventoryAuditReplayTaskApplicationServiceTest {

    private static final IdGenerator ID_GENERATOR = bizTag -> 1L;

    @Test
    void shouldCreateAndProcessReplayTask() {
        TestLogRepository repository = new TestLogRepository();
        BaconContextHolder.runWithTenantId(
                3001L,
                () -> repository.insertAuditDeadLetter(InventoryAuditDeadLetter.create(
                        DeadLetterId.of(101L),
                        com.github.thundax.bacon.inventory.domain.model.valueobject.OutboxId.of(101L),
                        EventCode.of("EVT20260326000000-000101"),
                        OrderNo.of("ORDER-1"),
                        ReservationNo.of("RSV-1"),
                        InventoryAuditActionType.RESERVE,
                        InventoryAuditOperatorType.SYSTEM,
                        String.valueOf(InventoryAuditLog.OPERATOR_ID_SYSTEM),
                        Instant.parse("2026-03-26T00:00:00Z"),
                        1,
                        "FAIL",
                        "MAX_RETRIES_EXCEEDED",
                        Instant.parse("2026-03-26T00:01:00Z"))));

        InventoryAuditReplayTaskApplicationService taskService =
                new InventoryAuditReplayTaskApplicationService(repository, ID_GENERATOR);
        InventoryAuditCompensationApplicationService compensationService =
                new InventoryAuditCompensationApplicationService(
                        repository,
                        new InventoryAuditReplayTransactionExecutor(
                                repository, repository, new InventoryTransactionExecutor(), ID_GENERATOR));

        var created = BaconContextHolder.callWithTenantId(
                3001L,
                () -> taskService.createReplayTask(9001L, "TASK-BATCH", List.of(101L)));
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

        private final Map<Long, InventoryAuditDeadLetter> deadLetters = new ConcurrentHashMap<>();
        private final Map<Long, InventoryAuditReplayTask> tasks = new ConcurrentHashMap<>();
        private final Map<Long, Long> taskTenants = new ConcurrentHashMap<>();
        private final Map<Long, List<InventoryAuditReplayTaskItem>> taskItems = new ConcurrentHashMap<>();
        private final List<InventoryAuditLog> auditLogs = new ArrayList<>();

        @Override
        public void insertAuditLog(InventoryAuditLog auditLog) {
            auditLogs.add(auditLog);
        }

        @Override
        public void insertLedger(InventoryLedger ledger) {}

        @Override
        public List<InventoryLedger> findLedgers(OrderNo orderNo) {
            return List.of();
        }

        @Override
        public List<InventoryAuditLog> findAuditLogs(OrderNo orderNo) {
            return List.copyOf(auditLogs);
        }

        @Override
        public void insertAuditDeadLetter(InventoryAuditDeadLetter deadLetter) {
            deadLetters.put(OutboxIdCodec.toValue(deadLetter.getOutboxId()), deadLetter);
        }

        @Override
        public Optional<InventoryAuditDeadLetter> findAuditDeadLetterById(DeadLetterId id) {
            return Optional.ofNullable(deadLetters.get(id.value()));
        }

        @Override
        public boolean claimAuditDeadLetterForReplay(
                DeadLetterId id,
                String replayKey,
                InventoryAuditOperatorType operatorType,
                OperatorId operatorId,
                Instant replayAt) {
            InventoryAuditDeadLetter deadLetter = deadLetters.get(id.value());
            if (deadLetter == null) {
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
        public InventoryAuditReplayTask insertAuditReplayTask(InventoryAuditReplayTask task) {
            assertNotNull(task.getId());
            Long taskId = task.getId() == null ? null : task.getId().value();
            tasks.put(taskId, task);
            taskTenants.put(
                    taskId,
                    java.util.Objects.requireNonNull(
                            BaconContextHolder.currentTenantId(), "tenantId must not be null"));
            return task;
        }

        @Override
        public void insertAuditReplayTaskItems(List<InventoryAuditReplayTaskItem> items) {
            if (items == null || items.isEmpty()) {
                return;
            }
            items.forEach(item -> assertNotNull(item.getId()));
            taskItems
                    .computeIfAbsent(
                            items.get(0).getTaskId() == null
                                    ? null
                                    : items.get(0).getTaskId().value(),
                            key -> new ArrayList<>())
                    .addAll(items);
        }

        @Override
        public Optional<InventoryAuditReplayTask> findAuditReplayTaskById(TaskId taskId) {
            return Optional.ofNullable(tasks.get(taskId == null ? null : taskId.value()));
        }

        @Override
        public Long findAuditReplayTaskTenantId(TaskId taskId) {
            return taskTenants.get(taskId == null ? null : taskId.value());
        }

        @Override
        public List<InventoryAuditReplayTask> claimRunnableAuditReplayTasks(
                Instant now, int limit, String processingOwner, Instant leaseUntil) {
            return tasks.values().stream()
                    .filter(task -> InventoryAuditReplayTaskStatus.PENDING.equals(task.getStatus())
                            || InventoryAuditReplayTaskStatus.RUNNING.equals(task.getStatus()))
                    .filter(task -> task.getLeaseUntil() == null
                            || !task.getLeaseUntil().isAfter(now))
                    .sorted(Comparator.comparing(
                            task -> task.getId() == null ? null : task.getId().value()))
                    .limit(limit)
                    .peek(task -> task.claim(processingOwner, leaseUntil, now))
                    .toList();
        }

        @Override
        public void renewAuditReplayTaskLease(
                TaskId taskId, String processingOwner, Instant leaseUntil, Instant updatedAt) {
            InventoryAuditReplayTask task = tasks.get(taskId == null ? null : taskId.value());
            if (task != null && processingOwner.equals(task.getProcessingOwner())) {
                task.renewLease(leaseUntil, updatedAt);
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
                        item.markResult(itemStatus, replayStatus, replayKey, resultMessage, startedAt, finishedAt);
                    }));
        }

        @Override
        public void updateAuditReplayTaskProgress(
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
            task.markItemProgress(processedDelta, successDelta, failedDelta, updatedAt);
        }

        @Override
        public void markAuditReplayTaskFinished(
                TaskId taskId, String processingOwner, String status, String lastError, Instant finishedAt) {
            InventoryAuditReplayTask task = tasks.get(taskId == null ? null : taskId.value());
            if (task == null || !processingOwner.equals(task.getProcessingOwner())) {
                return;
            }
            task.finish(InventoryAuditReplayTaskStatus.from(status), lastError, finishedAt);
        }
    }
}
