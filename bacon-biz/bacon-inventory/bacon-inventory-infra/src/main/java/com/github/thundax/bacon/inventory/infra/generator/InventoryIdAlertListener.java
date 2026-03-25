package com.github.thundax.bacon.inventory.infra.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InventoryIdAlertListener {

    @EventListener
    public void onFallback(InventoryIdFallbackEvent event) {
        log.error("ALERT inventory id provider degraded, operation={}, reason={}, occurredAt={}",
                event.operation(), event.reason(), event.occurredAt());
    }
}
