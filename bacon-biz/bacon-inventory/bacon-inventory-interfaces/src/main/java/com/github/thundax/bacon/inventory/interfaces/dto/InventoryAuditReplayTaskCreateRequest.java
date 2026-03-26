package com.github.thundax.bacon.inventory.interfaces.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record InventoryAuditReplayTaskCreateRequest(
        @NotNull @Positive Long tenantId,
        Long operatorId,
        String replayKeyPrefix,
        @NotEmpty List<@NotNull @Positive Long> deadLetterIds
) {
}
