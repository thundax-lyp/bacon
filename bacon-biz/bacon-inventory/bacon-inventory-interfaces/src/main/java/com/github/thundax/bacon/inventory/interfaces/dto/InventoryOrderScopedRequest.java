package com.github.thundax.bacon.inventory.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryOrderScopedRequest {

    @NotBlank
    private String orderNo;
}
