package com.github.thundax.bacon.inventory.infra.generator;

import java.time.Instant;

public record InventoryTinyIdFallbackEvent(String operation, String reason, Instant occurredAt) {
}
