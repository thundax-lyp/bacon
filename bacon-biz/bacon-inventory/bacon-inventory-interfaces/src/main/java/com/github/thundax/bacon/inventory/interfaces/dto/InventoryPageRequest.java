package com.github.thundax.bacon.inventory.interfaces.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryPageRequest {

    @NotNull
    @Positive
    private Long tenantId;

    @Positive
    private Long skuId;

    private String status;
    private Integer pageNo;
    private Integer pageSize;
}
