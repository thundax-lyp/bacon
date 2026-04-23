package com.github.thundax.bacon.inventory.interfaces.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryOrderScopedRequest {

    @NotBlank
    @Size(max = 64)
    private String orderNo;
}
