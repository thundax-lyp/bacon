package com.github.thundax.bacon.order.application.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.github.thundax.bacon.order.domain.model.valueobject.OrderIdempotencyRecordKey;
import org.junit.jupiter.api.Test;

class OrderCodecTest {

    @Test
    void shouldConvertSimpleValueObjects() {
        assertEquals("EVENT_1", EventCodeCodec.toValue(EventCodeCodec.toDomain("EVENT_1")));
        assertEquals(12L, OrderIdCodec.toValue(OrderIdCodec.toDomain(12L)));
        assertEquals(34L, OutboxIdCodec.toValue(OutboxIdCodec.toDomain(34L)));
        assertEquals("RSV-1", ReservationNoCodec.toValue(ReservationNoCodec.toDomain("RSV-1")));
    }

    @Test
    void shouldConvertOrderIdempotencyRecordKey() {
        OrderIdempotencyRecordKey key = OrderIdempotencyRecordKeyCodec.toDomain(1L, "ORD-1", "MARK_PAID");

        assertEquals(1L, OrderIdempotencyRecordKeyCodec.toTenantIdValue(key));
        assertEquals("ORD-1", OrderIdempotencyRecordKeyCodec.toOrderNoValue(key));
        assertEquals("MARK_PAID", OrderIdempotencyRecordKeyCodec.toEventTypeValue(key));
    }

    @Test
    void shouldHandleNull() {
        assertNull(EventCodeCodec.toDomain(null));
        assertNull(EventCodeCodec.toValue(null));
        assertNull(OrderIdCodec.toDomain(null));
        assertNull(OrderIdCodec.toValue(null));
        assertNull(OutboxIdCodec.toDomain(null));
        assertNull(OutboxIdCodec.toValue(null));
        assertNull(ReservationNoCodec.toDomain(null));
        assertNull(ReservationNoCodec.toValue(null));
        assertNull(OrderIdempotencyRecordKeyCodec.toDomain(null, null, null));
        assertNull(OrderIdempotencyRecordKeyCodec.toTenantIdValue(null));
        assertNull(OrderIdempotencyRecordKeyCodec.toOrderNoValue(null));
        assertNull(OrderIdempotencyRecordKeyCodec.toEventTypeValue(null));
    }
}
