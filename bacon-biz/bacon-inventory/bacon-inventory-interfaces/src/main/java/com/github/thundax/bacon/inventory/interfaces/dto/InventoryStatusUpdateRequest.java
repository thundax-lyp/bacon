package com.github.thundax.bacon.inventory.interfaces.dto;

import jakarta.validation.constraints.NotBlank;

public record InventoryStatusUpdateRequest(
        @NotBlank String status
) {
}
