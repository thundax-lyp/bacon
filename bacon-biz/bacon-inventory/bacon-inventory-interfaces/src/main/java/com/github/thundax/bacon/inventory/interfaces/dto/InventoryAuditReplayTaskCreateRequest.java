package com.github.thundax.bacon.inventory.interfaces.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public record InventoryAuditReplayTaskCreateRequest(
        Long operatorId, @Size(max = 64) String replayKeyPrefix, @NotEmpty List<@NotNull @Positive Long> deadLetterIds) {}
