package com.github.thundax.bacon.inventory.infra.repository.impl;

import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditDeadLetter;
import com.github.thundax.bacon.inventory.domain.entity.InventoryAuditOutbox;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryInventoryRepositoryImplTest {

    @Test
    void shouldSupportAuditOutboxRetryLifecycle() {
        InMemoryInventoryRepositoryImpl repository = new InMemoryInventoryRepositoryImpl();
        Instant now = Instant.parse("2026-03-26T10:00:00Z");

        repository.saveAuditOutbox(new InventoryAuditOutbox(null, 1001L, "ORDER-1", "RSV-1",
                "RESERVE", "SYSTEM", 0L, now, "DB_TIMEOUT", InventoryAuditOutbox.STATUS_NEW,
                0, now, null, null, null, null, now, now));

        List<InventoryAuditOutbox> retryable = repository.findRetryableAuditOutbox(now.plusSeconds(1), 10);
        assertEquals(1, retryable.size());
        Long outboxId = retryable.get(0).getId();

        Instant nextRetryAt = now.plusSeconds(300);
        repository.updateAuditOutboxForRetry(outboxId, 1, nextRetryAt, "RETRY_FAIL", now.plusSeconds(5));

        assertTrue(repository.findRetryableAuditOutbox(now.plusSeconds(200), 10).isEmpty());
        assertEquals(1, repository.findRetryableAuditOutbox(now.plusSeconds(301), 10).size());

        repository.markAuditOutboxDead(outboxId, 6, "MAX_RETRIES_EXCEEDED", now.plusSeconds(600));
        assertTrue(repository.findRetryableAuditOutbox(now.plusSeconds(601), 10).isEmpty());

        repository.saveAuditDeadLetter(new InventoryAuditDeadLetter(null, outboxId, 1001L, "ORDER-1", "RSV-1",
                "RESERVE", "SYSTEM", 0L, now, 6, "RETRY_FAIL", "MAX_RETRIES_EXCEEDED", now.plusSeconds(600)));

        repository.deleteAuditOutbox(outboxId);
        assertTrue(repository.findRetryableAuditOutbox(now.plusSeconds(1000), 10).isEmpty());
    }

    @Test
    void shouldClaimOutboxOnceAndRecycleExpiredLease() {
        InMemoryInventoryRepositoryImpl repository = new InMemoryInventoryRepositoryImpl();
        Instant now = Instant.parse("2026-03-26T10:00:00Z");
        repository.saveAuditOutbox(new InventoryAuditOutbox(null, 1001L, "ORDER-2", "RSV-2",
                "RESERVE", "SYSTEM", 0L, now, "INIT", InventoryAuditOutbox.STATUS_NEW,
                0, now, null, null, null, null, now, now));

        List<InventoryAuditOutbox> firstClaim = repository.claimRetryableAuditOutbox(now, 10, "owner-a",
                now.plusSeconds(30));
        List<InventoryAuditOutbox> secondClaim = repository.claimRetryableAuditOutbox(now, 10, "owner-b",
                now.plusSeconds(30));

        assertEquals(1, firstClaim.size());
        assertTrue(secondClaim.isEmpty());
        assertEquals(0, repository.releaseExpiredAuditOutboxLease(now.plusSeconds(10)));
        assertEquals(1, repository.releaseExpiredAuditOutboxLease(now.plusSeconds(31)));
        assertEquals(1, repository.findRetryableAuditOutbox(now.plusSeconds(31), 10).size());
    }
}
