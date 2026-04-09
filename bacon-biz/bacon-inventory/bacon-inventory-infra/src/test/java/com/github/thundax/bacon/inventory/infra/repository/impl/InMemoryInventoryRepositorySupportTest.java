package com.github.thundax.bacon.inventory.infra.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditDeadLetter;
import com.github.thundax.bacon.inventory.domain.model.entity.InventoryAuditOutbox;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditActionType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditOperatorType;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditOutboxStatus;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryAuditReplayStatus;
import com.github.thundax.bacon.inventory.domain.model.valueobject.OutboxId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservationNo;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class InMemoryInventoryRepositorySupportTest {

    @Test
    void shouldSupportAuditOutboxRetryLifecycle() {
        InMemoryInventoryRepositorySupport repository = new InMemoryInventoryRepositorySupport();
        Instant now = Instant.parse("2026-03-26T10:00:00Z");

        repository.saveAuditOutbox(new InventoryAuditOutbox(
                null,
                null,
                TenantId.of(1001L),
                OrderNo.of("ORDER-1"),
                ReservationNo.of("RSV-1"),
                InventoryAuditActionType.RESERVE,
                InventoryAuditOperatorType.SYSTEM,
                "0",
                now,
                "DB_TIMEOUT",
                InventoryAuditOutboxStatus.NEW,
                0,
                now,
                null,
                null,
                null,
                null,
                now,
                now));

        List<InventoryAuditOutbox> retryable = repository.findRetryableAuditOutbox(now.plusSeconds(1), 10);
        assertEquals(1, retryable.size());
        OutboxId outboxId = retryable.get(0).getId();

        Instant nextRetryAt = now.plusSeconds(300);
        repository.updateAuditOutboxForRetry(outboxId, 1, nextRetryAt, "RETRY_FAIL", now.plusSeconds(5));

        assertTrue(repository.findRetryableAuditOutbox(now.plusSeconds(200), 10).isEmpty());
        assertEquals(
                1, repository.findRetryableAuditOutbox(now.plusSeconds(301), 10).size());

        repository.markAuditOutboxDead(outboxId, 6, "MAX_RETRIES_EXCEEDED", now.plusSeconds(600));
        assertTrue(repository.findRetryableAuditOutbox(now.plusSeconds(601), 10).isEmpty());

        repository.saveAuditDeadLetter(InventoryAuditDeadLetter.create(
                outboxId,
                retryable.get(0).getEventCode(),
                TenantId.of(1001L),
                OrderNo.of("ORDER-1"),
                ReservationNo.of("RSV-1"),
                InventoryAuditActionType.RESERVE,
                InventoryAuditOperatorType.SYSTEM,
                "0",
                now,
                6,
                "RETRY_FAIL",
                "MAX_RETRIES_EXCEEDED",
                now.plusSeconds(600),
                InventoryAuditReplayStatus.PENDING,
                0,
                null,
                null,
                null,
                null,
                null,
                null));

        repository.deleteAuditOutbox(outboxId);
        assertTrue(
                repository.findRetryableAuditOutbox(now.plusSeconds(1000), 10).isEmpty());
    }

    @Test
    void shouldClaimOutboxOnceAndRecycleExpiredLease() {
        InMemoryInventoryRepositorySupport repository = new InMemoryInventoryRepositorySupport();
        Instant now = Instant.parse("2026-03-26T10:00:00Z");
        repository.saveAuditOutbox(new InventoryAuditOutbox(
                null,
                null,
                TenantId.of(1001L),
                OrderNo.of("ORDER-2"),
                ReservationNo.of("RSV-2"),
                InventoryAuditActionType.RESERVE,
                InventoryAuditOperatorType.SYSTEM,
                "0",
                now,
                "INIT",
                InventoryAuditOutboxStatus.NEW,
                0,
                now,
                null,
                null,
                null,
                null,
                now,
                now));

        List<InventoryAuditOutbox> firstClaim =
                repository.claimRetryableAuditOutbox(now, 10, "owner-a", now.plusSeconds(30));
        List<InventoryAuditOutbox> secondClaim =
                repository.claimRetryableAuditOutbox(now, 10, "owner-b", now.plusSeconds(30));

        assertEquals(1, firstClaim.size());
        assertTrue(secondClaim.isEmpty());
        assertEquals(0, repository.releaseExpiredAuditOutboxLease(now.plusSeconds(10)));
        assertEquals(1, repository.releaseExpiredAuditOutboxLease(now.plusSeconds(31)));
        assertEquals(
                1, repository.findRetryableAuditOutbox(now.plusSeconds(31), 10).size());
    }

    @Test
    void shouldRequireOwnerMatchWhenUpdatingClaimedOutbox() {
        InMemoryInventoryRepositorySupport repository = new InMemoryInventoryRepositorySupport();
        Instant now = Instant.parse("2026-03-26T10:00:00Z");
        repository.saveAuditOutbox(new InventoryAuditOutbox(
                null,
                null,
                TenantId.of(1001L),
                OrderNo.of("ORDER-3"),
                ReservationNo.of("RSV-3"),
                InventoryAuditActionType.RESERVE,
                InventoryAuditOperatorType.SYSTEM,
                "0",
                now,
                "INIT",
                InventoryAuditOutboxStatus.NEW,
                0,
                now,
                null,
                null,
                null,
                null,
                now,
                now));

        List<InventoryAuditOutbox> claimed =
                repository.claimRetryableAuditOutbox(now, 1, "owner-a", now.plusSeconds(30));
        OutboxId outboxId = claimed.get(0).getId();
        boolean wrongOwnerUpdated = repository.updateAuditOutboxForRetryClaimed(
                outboxId, "owner-b", 1, now.plusSeconds(60), "ERR", now.plusSeconds(5));
        boolean rightOwnerUpdated = repository.updateAuditOutboxForRetryClaimed(
                outboxId, "owner-a", 1, now.plusSeconds(60), "ERR", now.plusSeconds(5));

        assertFalse(wrongOwnerUpdated);
        assertTrue(rightOwnerUpdated);
    }
}
