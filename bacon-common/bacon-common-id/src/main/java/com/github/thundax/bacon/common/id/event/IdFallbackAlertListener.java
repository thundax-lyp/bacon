package com.github.thundax.bacon.common.id.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class IdFallbackAlertListener {

    @EventListener
    public void onFallback(IdFallbackEvent event) {
        log.error("ALERT id provider degraded, bizTag={}, operation={}, reason={}, occurredAt={}",
                event.bizTag(), event.operation(), event.reason(), event.occurredAt());
    }
}
