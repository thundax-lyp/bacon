package com.github.thundax.bacon.inventory.application;

import com.github.thundax.bacon.inventory.application.audit.InventoryAuditCompensationApplicationService;
import com.github.thundax.bacon.inventory.application.audit.InventoryAuditReplayTransactionExecutor;
import com.github.thundax.bacon.inventory.application.support.InventoryTransactionExecutor;
import com.github.thundax.bacon.inventory.api.dto.InventoryAuditReplayResultDTO;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditDeadLetter;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryLedger;
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
        repository.saveAuditDeadLetter(new InventoryAuditDeadLetter(1001L, 3001L, "ORDER-1", "RSV-1",
                InventoryAuditLog.ACTION_RESERVE, InventoryAuditLog.OPERATOR_TYPE_SYSTEM,
                InventoryAuditLog.OPERATOR_ID_SYSTEM, Instant.parse("2026-03-26T00:00:00Z"), 3, "FAIL",
                "MAX_RETRIES_EXCEEDED", Instant.parse("2026-03-26T00:01:00Z")));

        InventoryAuditReplayResultDTO result = service.replayDeadLetter(3001L, 1001L, "MANUAL-REPLAY-1001", 9001L);

        assertEquals(InventoryAuditDeadLetter.REPLAY_STATUS_SUCCEEDED, result.getReplayStatus());
        assertEquals("MANUAL-REPLAY-1001", result.getReplayKey());
        assertEquals(2, repository.auditLogs.size());
        assertEquals(InventoryAuditLog.ACTION_AUDIT_REPLAY_SUCCEEDED,
                repository.auditLogs.get(1).getActionType());
        assertEquals(InventoryAuditDeadLetter.REPLAY_STATUS_SUCCEEDED,
                repository.deadLetters.get(1001L).getReplayStatus());
    }

    @Test
    void shouldBatchReplayAndKeepRunningItemsUnchanged() {
        TestLogRepository repository = new TestLogRepository();
        InventoryAuditCompensationApplicationService service = createService(repository);
        repository.saveAuditDeadLetter(new InventoryAuditDeadLetter(1002L, 3001L, "ORDER-2", "RSV-2",
                InventoryAuditLog.ACTION_RELEASE, InventoryAuditLog.OPERATOR_TYPE_SYSTEM,
                InventoryAuditLog.OPERATOR_ID_SYSTEM, Instant.parse("2026-03-26T00:00:00Z"), 2, "FAIL",
                "MAX_RETRIES_EXCEEDED", Instant.parse("2026-03-26T00:01:00Z"),
                InventoryAuditDeadLetter.REPLAY_STATUS_PENDING, 0, null, null, null, null, null, null));
        repository.saveAuditDeadLetter(new InventoryAuditDeadLetter(1003L, 3001L, "ORDER-3", "RSV-3",
                InventoryAuditLog.ACTION_DEDUCT, InventoryAuditLog.OPERATOR_TYPE_SYSTEM,
                InventoryAuditLog.OPERATOR_ID_SYSTEM, Instant.parse("2026-03-26T00:00:00Z"), 2, "FAIL",
                "MAX_RETRIES_EXCEEDED", Instant.parse("2026-03-26T00:01:00Z"),
                InventoryAuditDeadLetter.REPLAY_STATUS_RUNNING, 0, null, null, null, null, null, null));

        List<InventoryAuditReplayResultDTO> results = service.replayDeadLettersBatch(3001L, List.of(1002L, 1003L),
                "BATCH-1", 9002L);

        assertEquals(2, results.size());
        assertEquals(InventoryAuditDeadLetter.REPLAY_STATUS_SUCCEEDED, results.get(0).getReplayStatus());
        assertTrue(results.get(1).getMessage().contains("not-claimable"));
    }

    @Test
    void shouldCompensateWhenReplayTransactionFails() {
        TestLogRepository repository = new TestLogRepository();
        InventoryAuditCompensationApplicationService service = createService(repository, new FailingOnceTransactionExecutor());
        repository.saveAuditDeadLetter(new InventoryAuditDeadLetter(1004L, 3001L, "ORDER-4", "RSV-4",
                InventoryAuditLog.ACTION_RESERVE, InventoryAuditLog.OPERATOR_TYPE_SYSTEM,
                InventoryAuditLog.OPERATOR_ID_SYSTEM, Instant.parse("2026-03-26T00:00:00Z"), 1, "FAIL",
                "MAX_RETRIES_EXCEEDED", Instant.parse("2026-03-26T00:01:00Z")));

        InventoryAuditReplayResultDTO result = service.replayDeadLetter(3001L, 1004L, "MANUAL-REPLAY-1004", 9001L);

        assertEquals(InventoryAuditDeadLetter.REPLAY_STATUS_FAILED, result.getReplayStatus());
        assertTrue(result.getMessage().startsWith("tx-failed:"));
        assertEquals(InventoryAuditDeadLetter.REPLAY_STATUS_FAILED, repository.deadLetters.get(1004L).getReplayStatus());
        assertEquals("MANUAL", repository.deadLetters.get(1004L).getReplayOperatorType());
        assertEquals(InventoryAuditLog.ACTION_AUDIT_REPLAY_FAILED,
                repository.auditLogs.get(repository.auditLogs.size() - 1).getActionType());
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
        public List<InventoryLedger> findLedgers(Long tenantId, String orderNo) {
            return List.of();
        }

        @Override
        public List<InventoryAuditLog> findAuditLogs(Long tenantId, String orderNo) {
            return List.copyOf(auditLogs);
        }

        @Override
        public void saveAuditDeadLetter(InventoryAuditDeadLetter deadLetter) {
            deadLetters.put(deadLetter.getOutboxIdValue(), deadLetter);
        }

        @Override
        public Optional<InventoryAuditDeadLetter> findAuditDeadLetterById(Long id) {
            return Optional.ofNullable(deadLetters.get(id));
        }

        @Override
        public boolean claimAuditDeadLetterForReplay(Long id, Long tenantId, String replayKey,
                                                     String operatorType, Long operatorId, Instant replayAt) {
            InventoryAuditDeadLetter deadLetter = deadLetters.get(id);
            if (deadLetter == null || !tenantId.equals(deadLetter.getTenantIdValue())) {
                return false;
            }
            if (!InventoryAuditDeadLetter.REPLAY_STATUS_PENDING.equals(deadLetter.getReplayStatus())
                    && !InventoryAuditDeadLetter.REPLAY_STATUS_FAILED.equals(deadLetter.getReplayStatus())) {
                return false;
            }
            deadLetter.setReplayStatus(InventoryAuditDeadLetter.REPLAY_STATUS_RUNNING);
            deadLetter.setReplayKey(replayKey);
            deadLetter.setReplayOperatorType(operatorType);
            deadLetter.setReplayOperatorId(String.valueOf(operatorId));
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
            deadLetter.setReplayOperatorId(String.valueOf(operatorId));
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
            deadLetter.setReplayOperatorId(String.valueOf(operatorId));
            deadLetter.setLastReplayAt(replayAt);
            deadLetter.setLastReplayResult("FAILED");
            deadLetter.setLastReplayError(replayError);
        }
    }
}
