package com.github.thundax.bacon.inventory.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.domain.OperatorId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.api.dto.InventoryAuditReplayResultDTO;
import com.github.thundax.bacon.inventory.application.audit.InventoryAuditCompensationApplicationService;
import com.github.thundax.bacon.inventory.application.audit.InventoryAuditReplayTransactionExecutor;
import com.github.thundax.bacon.inventory.application.codec.OutboxIdCodec;
import com.github.thundax.bacon.inventory.application.support.InventoryTransactionExecutor;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditDeadLetter;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditLog;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryLedger;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditActionType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditOperatorType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.DeadLetterId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.EventCode;
import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservationNo;
import com.github.thundax.bacon.inventory.domain.repository.InventoryLogRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class InventoryAuditCompensationApplicationServiceTest {

    private static final IdGenerator ID_GENERATOR = bizTag -> 1L;

    @Test
    void shouldReplayDeadLetterSuccessfully() {
        TestLogRepository repository = new TestLogRepository();
        InventoryAuditCompensationApplicationService service = createService(repository);
        BaconContextHolder.runWithTenantId(3001L, () -> repository.saveAuditDeadLetter(InventoryAuditDeadLetter.create(
                DeadLetterId.of(1001L),
                com.github.thundax.bacon.inventory.domain.model.valueobject.OutboxId.of(1001L),
                EventCode.of("EVT20260326000000-001001"),
                OrderNo.of("ORDER-1"),
                ReservationNo.of("RSV-1"),
                InventoryAuditActionType.RESERVE,
                InventoryAuditOperatorType.SYSTEM,
                String.valueOf(InventoryAuditLog.OPERATOR_ID_SYSTEM),
                Instant.parse("2026-03-26T00:00:00Z"),
                3,
                "FAIL",
                "MAX_RETRIES_EXCEEDED",
                Instant.parse("2026-03-26T00:01:00Z"))));

        InventoryAuditReplayResultDTO result = BaconContextHolder.callWithTenantId(
                3001L,
                () -> service.replayDeadLetter(DeadLetterId.of(1001L), "MANUAL-REPLAY-1001", OperatorId.of("9001")));

        assertEquals(InventoryAuditReplayStatus.SUCCEEDED.value(), result.getReplayStatus());
        assertEquals("MANUAL-REPLAY-1001", result.getReplayKey());
        assertEquals(2, repository.auditLogs.size());
        assertEquals(
                InventoryAuditActionType.AUDIT_REPLAY_SUCCEEDED.value(),
                repository.auditLogs.get(1).getActionType().value());
        assertEquals(
                InventoryAuditReplayStatus.SUCCEEDED,
                repository.deadLetters.get(1001L).getReplayStatus());
    }

    @Test
    void shouldBatchReplayAndKeepRunningItemsUnchanged() {
        TestLogRepository repository = new TestLogRepository();
        InventoryAuditCompensationApplicationService service = createService(repository);
        BaconContextHolder.runWithTenantId(3001L, () -> repository.saveAuditDeadLetter(InventoryAuditDeadLetter.create(
                DeadLetterId.of(1002L),
                com.github.thundax.bacon.inventory.domain.model.valueobject.OutboxId.of(1002L),
                EventCode.of("EVT20260326000000-001002"),
                OrderNo.of("ORDER-2"),
                ReservationNo.of("RSV-2"),
                InventoryAuditActionType.RELEASE,
                InventoryAuditOperatorType.SYSTEM,
                String.valueOf(InventoryAuditLog.OPERATOR_ID_SYSTEM),
                Instant.parse("2026-03-26T00:00:00Z"),
                2,
                "FAIL",
                "MAX_RETRIES_EXCEEDED",
                Instant.parse("2026-03-26T00:01:00Z"))));
        BaconContextHolder.runWithTenantId(3001L, () -> repository.saveAuditDeadLetter(InventoryAuditDeadLetter.create(
                DeadLetterId.of(1003L),
                com.github.thundax.bacon.inventory.domain.model.valueobject.OutboxId.of(1003L),
                EventCode.of("EVT20260326000000-001003"),
                OrderNo.of("ORDER-3"),
                ReservationNo.of("RSV-3"),
                InventoryAuditActionType.DEDUCT,
                InventoryAuditOperatorType.SYSTEM,
                String.valueOf(InventoryAuditLog.OPERATOR_ID_SYSTEM),
                Instant.parse("2026-03-26T00:00:00Z"),
                2,
                "FAIL",
                "MAX_RETRIES_EXCEEDED",
                Instant.parse("2026-03-26T00:01:00Z"))));
        repository.deadLetters.get(1003L).markReplayRunning("RUNNING-1003", InventoryAuditOperatorType.SYSTEM, "0", Instant.parse("2026-03-26T00:02:00Z"));

        List<InventoryAuditReplayResultDTO> results = BaconContextHolder.callWithTenantId(
                3001L,
                () -> service.replayDeadLettersBatch(
                        List.of(DeadLetterId.of(1002L), DeadLetterId.of(1003L)),
                        "BATCH-1",
                        OperatorId.of("9002")));

        assertEquals(2, results.size());
        assertEquals(
                InventoryAuditReplayStatus.SUCCEEDED.value(), results.get(0).getReplayStatus());
        assertTrue(results.get(1).getMessage().contains("not-claimable"));
    }

    @Test
    void shouldCompensateWhenReplayTransactionFails() {
        TestLogRepository repository = new TestLogRepository();
        InventoryAuditCompensationApplicationService service =
                createService(repository, new FailingOnceTransactionExecutor());
        BaconContextHolder.runWithTenantId(3001L, () -> repository.saveAuditDeadLetter(InventoryAuditDeadLetter.create(
                DeadLetterId.of(1004L),
                com.github.thundax.bacon.inventory.domain.model.valueobject.OutboxId.of(1004L),
                EventCode.of("EVT20260326000000-001004"),
                OrderNo.of("ORDER-4"),
                ReservationNo.of("RSV-4"),
                InventoryAuditActionType.RESERVE,
                InventoryAuditOperatorType.SYSTEM,
                String.valueOf(InventoryAuditLog.OPERATOR_ID_SYSTEM),
                Instant.parse("2026-03-26T00:00:00Z"),
                1,
                "FAIL",
                "MAX_RETRIES_EXCEEDED",
                Instant.parse("2026-03-26T00:01:00Z"))));

        InventoryAuditReplayResultDTO result = BaconContextHolder.callWithTenantId(
                3001L,
                () -> service.replayDeadLetter(DeadLetterId.of(1004L), "MANUAL-REPLAY-1004", OperatorId.of("9001")));

        assertEquals(InventoryAuditReplayStatus.FAILED.value(), result.getReplayStatus());
        assertTrue(result.getMessage().startsWith("tx-failed:"));
        assertEquals(
                InventoryAuditReplayStatus.FAILED,
                repository.deadLetters.get(1004L).getReplayStatus());
        assertEquals("MANUAL", repository.deadLetters.get(1004L).getReplayOperatorType());
        assertEquals(
                InventoryAuditActionType.AUDIT_REPLAY_FAILED.value(),
                repository.auditLogs.get(repository.auditLogs.size() - 1).getActionType().value());
    }

    private InventoryAuditCompensationApplicationService createService(TestLogRepository repository) {
        return createService(repository, new InventoryTransactionExecutor());
    }

    private InventoryAuditCompensationApplicationService createService(
            TestLogRepository repository, InventoryTransactionExecutor transactionExecutor) {
        InventoryAuditReplayTransactionExecutor service =
                new InventoryAuditReplayTransactionExecutor(
                        repository, repository, transactionExecutor, ID_GENERATOR);
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
        public void saveLedger(InventoryLedger ledger) {}

        @Override
        public List<InventoryLedger> findLedgers(OrderNo orderNo) {
            return List.of();
        }

        @Override
        public List<InventoryAuditLog> findAuditLogs(OrderNo orderNo) {
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
        public Optional<InventoryAuditDeadLetter> findAuditDeadLetterById(DeadLetterId id, TenantId tenantId) {
            return findAuditDeadLetterById(id);
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
    }
}
