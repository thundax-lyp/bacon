package com.github.thundax.bacon.inventory.interfaces.dto;

import com.github.thundax.bacon.inventory.domain.model.enums.InventoryStatus;
import jakarta.validation.constraints.NotNull;

public record InventoryStatusUpdateRequest(
        @NotNull InventoryStatus status
) {
}
