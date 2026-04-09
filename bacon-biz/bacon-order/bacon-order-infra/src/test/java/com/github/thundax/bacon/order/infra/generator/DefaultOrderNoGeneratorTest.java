package com.github.thundax.bacon.order.infra.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import org.junit.jupiter.api.Test;

class DefaultOrderNoGeneratorTest {

    @Test
    void nextOrderNoShouldUseTimestampAndSnowflakeTail() {
        IdGenerator idGenerator = bizTag -> {
            assertEquals("order", bizTag);
            return 123456789012345L;
        };
        DefaultOrderNoGenerator generator = new DefaultOrderNoGenerator(idGenerator);

        OrderNo orderNo = generator.nextOrderNo();

        assertTrue(orderNo.value().matches("ORD\\d{14}\\d{6}"));
        assertEquals("012345", orderNo.value().substring(orderNo.value().length() - 6));
    }
}
