package com.github.thundax.bacon.common.commerce.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class OrderNoCodecTest {

    @Test
    void shouldReturnNullForNullOrBlank() {
        assertNull(OrderNoCodec.toDomain(null));
        assertNull(OrderNoCodec.toDomain("   "));
        assertNull(OrderNoCodec.toValue(null));
    }

    @Test
    void shouldConvertOrderNo() {
        assertEquals("ORDER-001", OrderNoCodec.toDomain("ORDER-001").value());
        assertEquals("ORDER-002", OrderNoCodec.toValue(OrderNoCodec.toDomain("ORDER-002")));
    }
}
