package com.github.thundax.bacon.inventory.infra.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InventoryTinyIdAlertListener {

    @EventListener
    public void onFallback(InventoryTinyIdFallbackEvent event) {
        log.error("ALERT inventory tinyid degraded, operation={}, reason={}, occurredAt={}",
                event.operation(), event.reason(), event.occurredAt());
    }
}
