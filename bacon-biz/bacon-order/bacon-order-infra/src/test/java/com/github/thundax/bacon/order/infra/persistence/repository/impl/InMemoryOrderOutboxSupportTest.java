package com.github.thundax.bacon.order.infra.persistence.repository.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.thundax.bacon.order.domain.model.entity.OrderOutboxEvent;
import com.github.thundax.bacon.order.domain.model.enums.OrderOutboxEventType;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class InMemoryOrderOutboxSupportTest {

    @Test
    void saveOutboxEventShouldGenerateTechnicalIdAndBusinessEventCode() {
        InMemoryOrderOutboxSupport support = new InMemoryOrderOutboxSupport();
        OrderOutboxEvent event = OrderOutboxEvent.create(
                "ORD-1",
                OrderOutboxEventType.RESERVE_STOCK,
                "ORD-1:RESERVE",
                "{\"channelCode\":\"MOCK\"}",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Instant.now(),
                Instant.now());

        support.insert(event);
        List<OrderOutboxEvent> claimed = support.claim(
                Instant.now(), 10, "test-owner", Instant.now().plusSeconds(60));

        assertEquals(1, claimed.size());
        assertNull(event.getId());
        assertNull(event.getEventCode());
        assertNotNull(claimed.get(0).getId());
        assertNotNull(claimed.get(0).getEventCode());
        assertTrue(claimed.get(0).getEventCode().value().matches("^EVT\\d{14}-\\d{6}$"));
    }
}
