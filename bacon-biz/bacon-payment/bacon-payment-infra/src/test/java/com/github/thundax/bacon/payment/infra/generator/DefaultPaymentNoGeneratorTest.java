package com.github.thundax.bacon.payment.infra.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.thundax.bacon.common.id.core.IdGenerator;
import org.junit.jupiter.api.Test;

class DefaultPaymentNoGeneratorTest {

    @Test
    void nextPaymentNoShouldUseTimestampAndSnowflakeTail() {
        IdGenerator idGenerator = bizTag -> {
            assertEquals("payment", bizTag);
            return 123456789012345L;
        };
        DefaultPaymentNoGenerator generator = new DefaultPaymentNoGenerator(idGenerator);

        String paymentNo = generator.nextPaymentNo();

        assertTrue(paymentNo.matches("PAY\\d{14}\\d{6}"));
        assertEquals("012345", paymentNo.substring(paymentNo.length() - 6));
    }
}
