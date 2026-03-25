package com.github.thundax.bacon.inventory.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReservationItemDTO {

    @NotNull
    @Positive
    private Long skuId;
    @NotNull
    @Positive
    private Integer quantity;
}
