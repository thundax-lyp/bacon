package com.github.thundax.bacon.inventory.interfaces.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateInventoryRequest(
        @NotNull @Positive Long skuId, @NotNull @PositiveOrZero Integer onHandQuantity, @NotNull String status) {}
