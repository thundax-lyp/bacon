package com.github.thundax.bacon.inventory.domain.entity;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InventoryLedger {

    public static final String TYPE_RESERVE = "RESERVE";
    public static final String TYPE_RELEASE = "RELEASE";
    public static final String TYPE_DEDUCT = "DEDUCT";

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
