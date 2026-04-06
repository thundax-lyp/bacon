package com.github.thundax.bacon.order.infra.persistence.repository.impl;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.order.domain.model.entity.OrderOutboxEvent;
import com.github.thundax.bacon.order.domain.model.enums.OrderOutboxEventType;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderNo;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryOrderOutboxSupportTest {

    @Test
    void saveOutboxEventShouldGenerateTechnicalIdAndBusinessEventId() {
        InMemoryOrderOutboxSupport support = new InMemoryOrderOutboxSupport();
        OrderOutboxEvent event = new OrderOutboxEvent(null, null, TenantId.of(1001L), OrderNo.of("ORD-1"),
                OrderOutboxEventType.RESERVE_STOCK, "1001:ORD-1:RESERVE", "{\"channelCode\":\"MOCK\"}",
                null, null, null, null, null, null, null, null, Instant.now(), Instant.now());

        support.saveOutboxEvent(event);
        List<OrderOutboxEvent> claimed = support.claimRetryableOutbox(Instant.now(), 10, "test-owner",
                Instant.now().plusSeconds(60));

        assertNotNull(event.getId());
        assertNotNull(event.getEventId());
        assertTrue(event.getEventIdValue().startsWith("EVT"));
        assertEquals(1, claimed.size());
        assertEquals(event.getId(), claimed.get(0).getId());
        assertEquals(event.getEventIdValue(), claimed.get(0).getEventIdValue());
    }
}
