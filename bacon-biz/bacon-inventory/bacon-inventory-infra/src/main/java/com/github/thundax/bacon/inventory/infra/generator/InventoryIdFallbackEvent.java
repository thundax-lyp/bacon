package com.github.thundax.bacon.inventory.infra.generator;

import java.time.Instant;

public record InventoryIdFallbackEvent(String operation, String reason, Instant occurredAt) {
}
