package com.github.thundax.bacon.inventory.application;

import com.github.thundax.bacon.inventory.application.audit.InventoryAuditCompensationApplicationService;
import com.github.thundax.bacon.inventory.application.audit.InventoryAuditReplayTransactionExecutor;
import com.github.thundax.bacon.inventory.application.support.InventoryTransactionExecutor;
import com.github.thundax.bacon.inventory.api.dto.InventoryAuditReplayResultDTO;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditDeadLetter;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryLedger;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditActionType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditOperatorType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.DeadLetterId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.EventCode;
import com.github.thundax.bacon.inventory.domain.model.valueobject.OrderNo;
import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservationNo;
import com.github.thundax.bacon.common.id.domain.OperatorId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.domain.repository.InventoryLogRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InventoryAuditCompensationApplicationServiceTest {

    @Test
    void shouldReplayDeadLetterSuccessfully() {
        TestLogRepository repository = new TestLogRepository();
        InventoryAuditCompensationApplicationService service = createService(repository);
        repository.saveAuditDeadLetter(new InventoryAuditDeadLetter(null, com.github.thundax.bacon.inventory.domain.model.valueobject.OutboxId.of(1001L), EventCode.of("EVT20260326000000-001001"),
                TenantId.of(3001L), OrderNo.of("ORDER-1"), ReservationNo.of("RSV-1"), InventoryAuditActionType.RESERVE,
                InventoryAuditOperatorType.SYSTEM, String.valueOf(InventoryAuditLog.OPERATOR_ID_SYSTEM),
                Instant.parse("2026-03-26T00:00:00Z"), 3, "FAIL",
                "MAX_RETRIES_EXCEEDED", Instant.parse("2026-03-26T00:01:00Z"), InventoryAuditReplayStatus.PENDING,
                0, null, null, null, null, null, null));

        InventoryAuditReplayResultDTO result = service.replayDeadLetter(TenantId.of(3001L), DeadLetterId.of(1001L),
                "MANUAL-REPLAY-1001", OperatorId.of("9001"));

        assertEquals(InventoryAuditReplayStatus.SUCCEEDED.value(), result.getReplayStatus());
        assertEquals("MANUAL-REPLAY-1001", result.getReplayKey());
        assertEquals(2, repository.auditLogs.size());
        assertEquals(InventoryAuditActionType.AUDIT_REPLAY_SUCCEEDED.value(),
                repository.auditLogs.get(1).getActionTypeValue());
        assertEquals(InventoryAuditReplayStatus.SUCCEEDED,
                repository.deadLetters.get(1001L).getReplayStatus());
    }

    @Test
    void shouldBatchReplayAndKeepRunningItemsUnchanged() {
        TestLogRepository repository = new TestLogRepository();
        InventoryAuditCompensationApplicationService service = createService(repository);
        repository.saveAuditDeadLetter(new InventoryAuditDeadLetter(null, com.github.thundax.bacon.inventory.domain.model.valueobject.OutboxId.of(1002L), EventCode.of("EVT20260326000000-001002"),
                TenantId.of(3001L), OrderNo.of("ORDER-2"), ReservationNo.of("RSV-2"), InventoryAuditActionType.RELEASE,
                InventoryAuditOperatorType.SYSTEM, String.valueOf(InventoryAuditLog.OPERATOR_ID_SYSTEM),
                Instant.parse("2026-03-26T00:00:00Z"), 2, "FAIL", "MAX_RETRIES_EXCEEDED", Instant.parse("2026-03-26T00:01:00Z"),
                InventoryAuditReplayStatus.PENDING, 0, null, null, null, null, null, null));
        repository.saveAuditDeadLetter(new InventoryAuditDeadLetter(null, com.github.thundax.bacon.inventory.domain.model.valueobject.OutboxId.of(1003L), EventCode.of("EVT20260326000000-001003"),
                TenantId.of(3001L), OrderNo.of("ORDER-3"), ReservationNo.of("RSV-3"), InventoryAuditActionType.DEDUCT,
                InventoryAuditOperatorType.SYSTEM, String.valueOf(InventoryAuditLog.OPERATOR_ID_SYSTEM),
                Instant.parse("2026-03-26T00:00:00Z"), 2, "FAIL", "MAX_RETRIES_EXCEEDED", Instant.parse("2026-03-26T00:01:00Z"),
                InventoryAuditReplayStatus.RUNNING, 0, null, null, null, null, null, null));

        List<InventoryAuditReplayResultDTO> results = service.replayDeadLettersBatch(TenantId.of(3001L),
                List.of(DeadLetterId.of(1002L), DeadLetterId.of(1003L)), "BATCH-1", OperatorId.of("9002"));

        assertEquals(2, results.size());
        assertEquals(InventoryAuditReplayStatus.SUCCEEDED.value(), results.get(0).getReplayStatus());
        assertTrue(results.get(1).getMessage().contains("not-claimable"));
    }

    @Test
    void shouldCompensateWhenReplayTransactionFails() {
        TestLogRepository repository = new TestLogRepository();
        InventoryAuditCompensationApplicationService service = createService(repository, new FailingOnceTransactionExecutor());
        repository.saveAuditDeadLetter(new InventoryAuditDeadLetter(null, com.github.thundax.bacon.inventory.domain.model.valueobject.OutboxId.of(1004L), EventCode.of("EVT20260326000000-001004"),
                TenantId.of(3001L), OrderNo.of("ORDER-4"), ReservationNo.of("RSV-4"), InventoryAuditActionType.RESERVE,
                InventoryAuditOperatorType.SYSTEM, String.valueOf(InventoryAuditLog.OPERATOR_ID_SYSTEM),
                Instant.parse("2026-03-26T00:00:00Z"), 1, "FAIL",
                "MAX_RETRIES_EXCEEDED", Instant.parse("2026-03-26T00:01:00Z"), InventoryAuditReplayStatus.PENDING,
                0, null, null, null, null, null, null));

        InventoryAuditReplayResultDTO result = service.replayDeadLetter(TenantId.of(3001L), DeadLetterId.of(1004L),
                "MANUAL-REPLAY-1004", OperatorId.of("9001"));

        assertEquals(InventoryAuditReplayStatus.FAILED.value(), result.getReplayStatus());
        assertTrue(result.getMessage().startsWith("tx-failed:"));
        assertEquals(InventoryAuditReplayStatus.FAILED, repository.deadLetters.get(1004L).getReplayStatus());
        assertEquals("MANUAL", repository.deadLetters.get(1004L).getReplayOperatorType());
        assertEquals(InventoryAuditActionType.AUDIT_REPLAY_FAILED.value(),
                repository.auditLogs.get(repository.auditLogs.size() - 1).getActionTypeValue());
    }

    private InventoryAuditCompensationApplicationService createService(TestLogRepository repository) {
        return createService(repository, new InventoryTransactionExecutor());
    }

    private InventoryAuditCompensationApplicationService createService(TestLogRepository repository,
                                                            InventoryTransactionExecutor transactionExecutor) {
        InventoryAuditReplayTransactionExecutor service =
                new InventoryAuditReplayTransactionExecutor(repository, repository, transactionExecutor);
        return new InventoryAuditCompensationApplicationService(repository, service);
    }

    private static final class FailingOnceTransactionExecutor extends InventoryTransactionExecutor {

        private final AtomicInteger invokeCount = new AtomicInteger(0);

        @Override
        public <T> T executeInNewTransaction(java.util.function.Supplier<T> action) {
            if (invokeCount.incrementAndGet() == 1) {
                throw new RuntimeException("simulated-tx-error");
            }
            return action.get();
        }
    }

    private static final class TestLogRepository implements InventoryLogRepository {

        private final Map<Long, InventoryAuditDeadLetter> deadLetters = new ConcurrentHashMap<>();
        private final List<InventoryAuditLog> auditLogs = new ArrayList<>();

        @Override
        public void saveAuditLog(InventoryAuditLog auditLog) {
            auditLogs.add(auditLog);
        }

        @Override
        public void saveLedger(InventoryLedger ledger) {
        }

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
            deadLetters.put(deadLetter.getOutboxIdValue(), deadLetter);
        }

        @Override
        public Optional<InventoryAuditDeadLetter> findAuditDeadLetterById(DeadLetterId id) {
            return Optional.ofNullable(deadLetters.get(id.value()));
        }

        @Override
        public boolean claimAuditDeadLetterForReplay(DeadLetterId id, TenantId tenantId, String replayKey,
                                                     InventoryAuditOperatorType operatorType, OperatorId operatorId,
                                                     Instant replayAt) {
            InventoryAuditDeadLetter deadLetter = deadLetters.get(id.value());
            if (deadLetter == null || !tenantId.equals(deadLetter.getTenantId())) {
                return false;
            }
            if (!InventoryAuditReplayStatus.PENDING.equals(deadLetter.getReplayStatus())
                    && !InventoryAuditReplayStatus.FAILED.equals(deadLetter.getReplayStatus())) {
                return false;
            }
            deadLetter.setReplayStatus(InventoryAuditReplayStatus.RUNNING);
            deadLetter.setReplayKey(replayKey);
            deadLetter.setReplayOperatorType(operatorType == null ? null : operatorType.value());
            deadLetter.setReplayOperatorId(operatorId == null ? null : operatorId.value());
            deadLetter.setLastReplayAt(replayAt);
            deadLetter.setLastReplayResult("RUNNING");
            deadLetter.setLastReplayError(null);
            return true;
        }

        @Override
        public void markAuditDeadLetterReplaySuccess(DeadLetterId id, String replayKey,
                                                     InventoryAuditOperatorType operatorType, OperatorId operatorId,
                                                     Instant replayAt) {
            InventoryAuditDeadLetter deadLetter = deadLetters.get(id.value());
            deadLetter.setReplayStatus(InventoryAuditReplayStatus.SUCCEEDED);
            deadLetter.setReplayCount((deadLetter.getReplayCount() == null ? 0 : deadLetter.getReplayCount()) + 1);
            deadLetter.setReplayKey(replayKey);
            deadLetter.setReplayOperatorType(operatorType == null ? null : operatorType.value());
            deadLetter.setReplayOperatorId(operatorId == null ? null : operatorId.value());
            deadLetter.setLastReplayAt(replayAt);
            deadLetter.setLastReplayResult("SUCCEEDED");
            deadLetter.setLastReplayError(null);
        }

        @Override
        public void markAuditDeadLetterReplayFailed(DeadLetterId id, String replayKey,
                                                    InventoryAuditOperatorType operatorType, OperatorId operatorId,
                                                    String replayError, Instant replayAt) {
            InventoryAuditDeadLetter deadLetter = deadLetters.get(id.value());
            deadLetter.setReplayStatus(InventoryAuditReplayStatus.FAILED);
            deadLetter.setReplayCount((deadLetter.getReplayCount() == null ? 0 : deadLetter.getReplayCount()) + 1);
            deadLetter.setReplayKey(replayKey);
            deadLetter.setReplayOperatorType(operatorType == null ? null : operatorType.value());
            deadLetter.setReplayOperatorId(operatorId == null ? null : operatorId.value());
            deadLetter.setLastReplayAt(replayAt);
            deadLetter.setLastReplayResult("FAILED");
            deadLetter.setLastReplayError(replayError);
        }
    }
}
