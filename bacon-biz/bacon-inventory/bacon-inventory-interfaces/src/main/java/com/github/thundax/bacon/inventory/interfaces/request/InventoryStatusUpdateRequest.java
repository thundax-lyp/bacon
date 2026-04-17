package com.github.thundax.bacon.inventory.interfaces.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InventoryStatusUpdateRequest(@NotNull @Size(max = 32) String status) {}
