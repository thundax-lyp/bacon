package com.github.thundax.bacon.inventory.interfaces.dto;

import jakarta.validation.constraints.NotNull;

public record InventoryStatusUpdateRequest(@NotNull String status) {}
