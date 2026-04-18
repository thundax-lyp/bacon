package com.github.thundax.bacon.order.domain.model.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.order.domain.exception.OrderDomainException;
import com.github.thundax.bacon.order.domain.model.enums.OrderIdempotencyStatus;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderIdempotencyRecordKey;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class OrderIdempotencyRecordTest {

    @Test
    void createAndStartProcessingShouldInitializeRecord() {
        Instant now = Instant.parse("2026-04-18T10:00:00Z");
        OrderIdempotencyRecord record =
                OrderIdempotencyRecord.create(OrderIdempotencyRecordKey.of(OrderNo.of("ORD-001"), "PAY"), null, null, now);

        record.startProcessing(now.plusSeconds(1));

        assertEquals(OrderIdempotencyStatus.PROCESSING, record.getStatus());
        assertEquals(1, record.getAttemptCount());
        assertNull(record.getLastError());
        assertEquals(now.plusSeconds(1), record.getUpdatedAt());
    }

    @Test
    void recoverShouldTakeFailedRecordBackToProcessing() {
        Instant now = Instant.parse("2026-04-18T10:00:00Z");
        OrderIdempotencyRecord record = OrderIdempotencyRecord.reconstruct(
                OrderIdempotencyRecordKey.of(OrderNo.of("ORD-001"), "PAY"),
                OrderIdempotencyStatus.FAILED,
                1,
                "boom",
                null,
                null,
                null,
                now,
                now);

        record.recover("node-a", now.plusSeconds(30), now, now.plusSeconds(1));

        assertEquals(OrderIdempotencyStatus.PROCESSING, record.getStatus());
        assertEquals(2, record.getAttemptCount());
        assertNull(record.getLastError());
        assertEquals("node-a", record.getProcessingOwner());
    }

    @Test
    void expireShouldOnlyWorkForExpiredProcessingRecord() {
        Instant now = Instant.parse("2026-04-18T10:00:00Z");
        OrderIdempotencyRecord record = OrderIdempotencyRecord.reconstruct(
                OrderIdempotencyRecordKey.of(OrderNo.of("ORD-001"), "PAY"),
                OrderIdempotencyStatus.PROCESSING,
                1,
                null,
                "node-a",
                now.minusSeconds(1),
                now.minusSeconds(10),
                now.minusSeconds(20),
                now.minusSeconds(10));

        record.expire("lease expired", now);

        assertTrue(record.isFailed());
        assertEquals("lease expired", record.getLastError());
        assertNull(record.getProcessingOwner());
    }

    @Test
    void markSuccessShouldRequireProcessingStatus() {
        OrderIdempotencyRecord record = OrderIdempotencyRecord.reconstruct(
                OrderIdempotencyRecordKey.of(OrderNo.of("ORD-001"), "PAY"),
                OrderIdempotencyStatus.READY,
                0,
                null,
                null,
                null,
                null,
                Instant.now(),
                Instant.now());

        assertThrows(OrderDomainException.class, () -> record.markSuccess(Instant.now()));
    }

    @Test
    void startProcessingShouldRejectNonReadyStatus() {
        OrderIdempotencyRecord record = OrderIdempotencyRecord.reconstruct(
                OrderIdempotencyRecordKey.of(OrderNo.of("ORD-001"), "PAY"),
                OrderIdempotencyStatus.FAILED,
                1,
                "boom",
                null,
                null,
                null,
                Instant.now(),
                Instant.now());

        assertThrows(OrderDomainException.class, () -> record.startProcessing(Instant.now()));
    }

    @Test
    void recoverShouldRejectNonFailedStatus() {
        OrderIdempotencyRecord record = OrderIdempotencyRecord.reconstruct(
                OrderIdempotencyRecordKey.of(OrderNo.of("ORD-001"), "PAY"),
                OrderIdempotencyStatus.PROCESSING,
                1,
                null,
                "node-a",
                Instant.now().plusSeconds(30),
                Instant.now(),
                Instant.now(),
                Instant.now());

        assertThrows(
                OrderDomainException.class,
                () -> record.recover("node-b", Instant.now().plusSeconds(60), Instant.now(), Instant.now()));
    }
}
