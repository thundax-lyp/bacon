package com.github.thundax.bacon.inventory.domain.entity;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Inventory {

    private Long id;
    private Long tenantId;
    private Long skuId;
    private Long warehouseId;
    private Integer onHandQuantity;
    private Integer reservedQuantity;
    private Integer availableQuantity;
    private String status;
    private Instant updatedAt;
}
