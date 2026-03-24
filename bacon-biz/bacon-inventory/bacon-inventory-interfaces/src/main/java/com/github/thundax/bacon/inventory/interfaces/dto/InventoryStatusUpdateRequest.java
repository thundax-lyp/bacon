package com.github.thundax.bacon.inventory.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record InventoryStatusUpdateRequest(
        @NotNull @Positive Long tenantId,
        @NotBlank String status
) {
}
