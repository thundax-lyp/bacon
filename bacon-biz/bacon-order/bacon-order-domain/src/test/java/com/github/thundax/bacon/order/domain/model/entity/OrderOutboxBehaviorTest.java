package com.github.thundax.bacon.order.domain.model.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.thundax.bacon.order.domain.exception.OrderDomainException;
import com.github.thundax.bacon.order.domain.model.enums.OrderOutboxEventType;
import com.github.thundax.bacon.order.domain.model.enums.OrderOutboxReplayStatus;
import com.github.thundax.bacon.order.domain.model.enums.OrderOutboxStatus;
import com.github.thundax.bacon.order.domain.model.valueobject.EventCode;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderOutboxDeadLetterId;
import com.github.thundax.bacon.order.domain.model.valueobject.OutboxId;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class OrderOutboxBehaviorTest {

    @Test
    void outboxEventShouldClaimRetryAndDeadCorrectly() {
        Instant now = Instant.parse("2026-04-18T10:00:00Z");
        OrderOutboxEvent event = OrderOutboxEvent.reconstruct(
                OutboxId.of(1L),
                EventCode.of("EVT-001"),
                com.github.thundax.bacon.common.commerce.valueobject.OrderNo.of("ORD-001"),
                OrderOutboxEventType.RESERVE_STOCK,
                "biz-key",
                "{\"x\":1}",
                OrderOutboxStatus.NEW,
                0,
                null,
                null,
                null,
                null,
                null,
                null,
                now,
                now);

        event.claim("node-a", now.plusSeconds(30), now);
        event.markRetrying("node-a", now.plusSeconds(60), "boom", now.plusSeconds(1));
        event.claim("node-a", now.plusSeconds(90), now.plusSeconds(61));
        event.markDead("node-a", "retry exhausted", "boom again", now.plusSeconds(62));

        assertEquals(OrderOutboxStatus.DEAD, event.getStatus());
        assertEquals(2, event.getRetryCount());
        assertEquals("retry exhausted", event.getDeadReason());
        assertNull(event.getProcessingOwner());
    }

    @Test
    void releaseExpiredLeaseShouldRejectActiveLease() {
        Instant now = Instant.parse("2026-04-18T10:00:00Z");
        OrderOutboxEvent event = OrderOutboxEvent.reconstruct(
                OutboxId.of(1L),
                EventCode.of("EVT-001"),
                com.github.thundax.bacon.common.commerce.valueobject.OrderNo.of("ORD-001"),
                OrderOutboxEventType.RESERVE_STOCK,
                "biz-key",
                "{}",
                OrderOutboxStatus.PROCESSING,
                0,
                null,
                "node-a",
                now.plusSeconds(30),
                now,
                null,
                null,
                now.minusSeconds(1),
                now);

        assertThrows(OrderDomainException.class, () -> event.releaseExpiredLease(now));
    }

    @Test
    void createShouldApplyDefaultsAndNormalizeBlankMessage() {
        Instant now = Instant.parse("2026-04-18T10:00:00Z");
        OrderOutboxEvent event = OrderOutboxEvent.create(
                "ORD-001", OrderOutboxEventType.RESERVE_STOCK, "biz-key", "{\"x\":1}", null, null, null, null, null, null, " ", null, now, null);

        assertEquals(OrderOutboxStatus.NEW, event.getStatus());
        assertEquals(0, event.getRetryCount());
        assertEquals(now, event.getCreatedAt());
        assertEquals(now, event.getUpdatedAt());
        assertNull(event.getErrorMessage());
    }

    @Test
    void deadLetterShouldTrackReplayAndRebuildDeadEvent() {
        Instant now = Instant.parse("2026-04-18T10:00:00Z");
        OrderOutboxEvent event = OrderOutboxEvent.reconstruct(
                OutboxId.of(1L),
                EventCode.of("EVT-001"),
                com.github.thundax.bacon.common.commerce.valueobject.OrderNo.of("ORD-001"),
                OrderOutboxEventType.CREATE_PAYMENT,
                "biz-key",
                "{\"pay\":1}",
                OrderOutboxStatus.DEAD,
                3,
                null,
                null,
                null,
                null,
                "boom",
                "retry exhausted",
                now.minusSeconds(60),
                now.minusSeconds(30));
        OrderOutboxDeadLetter deadLetter =
                OrderOutboxDeadLetter.create(OrderOutboxDeadLetterId.of(1L), event, 3, "boom", "retry exhausted", now);

        deadLetter.markReplayFailed(now.plusSeconds(1), "failed");
        deadLetter.markReplayPending("retry later", now.plusSeconds(2));
        deadLetter.markReplaySucceeded(now.plusSeconds(3), "ok");
        OrderOutboxEvent rebuilt = deadLetter.rebuildEvent();

        assertEquals(OrderOutboxReplayStatus.SUCCESS, deadLetter.getReplayStatus());
        assertEquals(2, deadLetter.getReplayCount());
        assertTrue(deadLetter.isReplaySucceeded());
        assertEquals(OrderOutboxStatus.DEAD, rebuilt.getStatus());
        assertEquals(event.getOrderNo(), rebuilt.getOrderNo());
    }

    @Test
    void markReplayPendingShouldRequireFailedStatus() {
        OrderOutboxDeadLetter deadLetter = OrderOutboxDeadLetter.reconstruct(
                OrderOutboxDeadLetterId.of(1L),
                OutboxId.of(1L),
                EventCode.of("EVT-001"),
                com.github.thundax.bacon.common.commerce.valueobject.OrderNo.of("ORD-001"),
                OrderOutboxEventType.CREATE_PAYMENT,
                "biz-key",
                "{}",
                1,
                "boom",
                "dead",
                Instant.now(),
                OrderOutboxReplayStatus.PENDING,
                0,
                null,
                null,
                Instant.now(),
                Instant.now());

        assertThrows(OrderDomainException.class, () -> deadLetter.markReplayPending("retry later", Instant.now()));
    }

    @Test
    void deadLetterCreateShouldNormalizeBlankErrorMessageToUnknown() {
        OrderOutboxEvent event = OrderOutboxEvent.reconstruct(
                OutboxId.of(1L),
                EventCode.of("EVT-001"),
                com.github.thundax.bacon.common.commerce.valueobject.OrderNo.of("ORD-001"),
                OrderOutboxEventType.CREATE_PAYMENT,
                "biz-key",
                "{}",
                OrderOutboxStatus.DEAD,
                1,
                null,
                null,
                null,
                null,
                null,
                "dead",
                Instant.now(),
                Instant.now());

        OrderOutboxDeadLetter deadLetter =
                OrderOutboxDeadLetter.create(OrderOutboxDeadLetterId.of(1L), event, 1, " ", "dead", Instant.now());

        assertEquals("UNKNOWN", deadLetter.getErrorMessage());
    }

    @Test
    void claimShouldRejectNonClaimableStatus() {
        Instant now = Instant.parse("2026-04-18T10:00:00Z");
        OrderOutboxEvent event = OrderOutboxEvent.reconstruct(
                OutboxId.of(1L),
                EventCode.of("EVT-001"),
                com.github.thundax.bacon.common.commerce.valueobject.OrderNo.of("ORD-001"),
                OrderOutboxEventType.RESERVE_STOCK,
                "biz-key",
                "{}",
                OrderOutboxStatus.DEAD,
                1,
                null,
                null,
                null,
                null,
                "boom",
                "dead",
                now,
                now);

        assertThrows(OrderDomainException.class, () -> event.claim("node-a", now.plusSeconds(30), now));
    }

    @Test
    void markRetryingShouldRejectWrongOwner() {
        Instant now = Instant.parse("2026-04-18T10:00:00Z");
        OrderOutboxEvent event = OrderOutboxEvent.reconstruct(
                OutboxId.of(1L),
                EventCode.of("EVT-001"),
                com.github.thundax.bacon.common.commerce.valueobject.OrderNo.of("ORD-001"),
                OrderOutboxEventType.RESERVE_STOCK,
                "biz-key",
                "{}",
                OrderOutboxStatus.PROCESSING,
                0,
                null,
                "node-a",
                now.plusSeconds(30),
                now,
                null,
                null,
                now.minusSeconds(1),
                now);

        assertThrows(
                OrderDomainException.class,
                () -> event.markRetrying("node-b", now.plusSeconds(60), "boom", now.plusSeconds(1)));
    }
}
