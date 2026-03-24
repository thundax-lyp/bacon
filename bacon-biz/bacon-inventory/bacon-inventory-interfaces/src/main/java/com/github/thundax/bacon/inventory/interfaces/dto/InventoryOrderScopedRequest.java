package com.github.thundax.bacon.inventory.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryOrderScopedRequest {

    @NotNull
    @Positive
    private Long tenantId;

    @NotBlank
    private String orderNo;
}
