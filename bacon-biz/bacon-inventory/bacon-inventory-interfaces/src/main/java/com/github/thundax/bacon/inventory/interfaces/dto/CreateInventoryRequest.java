package com.github.thundax.bacon.inventory.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateInventoryRequest(
        @NotNull @Positive Long tenantId,
        @NotNull @Positive Long skuId,
        @NotNull @Positive Integer onHandQuantity,
        @NotBlank String status
) {
}
