package com.github.thundax.bacon.inventory.interfaces.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record InventoryAuditReplayTaskControlRequest(
        @NotNull @Positive Long tenantId,
        Long operatorId
) {
}
