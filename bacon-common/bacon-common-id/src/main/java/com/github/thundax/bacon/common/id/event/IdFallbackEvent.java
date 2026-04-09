package com.github.thundax.bacon.common.id.event;

import java.time.Instant;

public record IdFallbackEvent(String bizTag, String operation, String reason, Instant occurredAt) {}
