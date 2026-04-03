package com.github.thundax.bacon.order.infra.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.thundax.bacon.common.id.core.IdGenerator;
import org.junit.jupiter.api.Test;

class DefaultOrderNoGeneratorTest {

    @Test
    void nextOrderNoShouldUseTimestampAndSnowflakeTail() {
        IdGenerator idGenerator = bizTag -> {
            assertEquals("order", bizTag);
            return 123456789012345L;
        };
        DefaultOrderNoGenerator generator = new DefaultOrderNoGenerator(idGenerator);

        String orderNo = generator.nextOrderNo();

        assertTrue(orderNo.matches("ORD\\d{14}\\d{6}"));
        assertEquals("012345", orderNo.substring(orderNo.length() - 6));
    }
}
