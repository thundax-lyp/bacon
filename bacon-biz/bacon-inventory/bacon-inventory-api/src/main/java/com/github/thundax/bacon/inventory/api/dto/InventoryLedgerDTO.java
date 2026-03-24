package com.github.thundax.bacon.inventory.api.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryLedgerDTO {

    private Long id;
    private Long tenantId;
    private String orderNo;
    private String reservationNo;
    private Long skuId;
    private Long warehouseId;
    private String ledgerType;
    private Integer quantity;
    private Instant occurredAt;
}
