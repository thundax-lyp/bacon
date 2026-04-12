package com.github.thundax.bacon.common.commerce.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class PaymentNoCodecTest {

    @Test
    void shouldReturnNullForNullOrBlank() {
        assertNull(PaymentNoCodec.toDomain(null));
        assertNull(PaymentNoCodec.toDomain("   "));
        assertNull(PaymentNoCodec.toValue(null));
    }

    @Test
    void shouldConvertPaymentNo() {
        assertEquals("PAY-001", PaymentNoCodec.toDomain("PAY-001").value());
        assertEquals("PAY-002", PaymentNoCodec.toValue(PaymentNoCodec.toDomain("PAY-002")));
    }
}
