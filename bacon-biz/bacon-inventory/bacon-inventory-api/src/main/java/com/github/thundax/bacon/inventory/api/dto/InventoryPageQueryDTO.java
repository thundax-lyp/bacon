package com.github.thundax.bacon.inventory.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryPageQueryDTO {

    private Long tenantId;
    private Long skuId;
    private String status;
    private Integer pageNo;
    private Integer pageSize;
}
