package com.github.thundax.bacon.inventory.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InventoryReservationItem {

    private Long id;
    private Long tenantId;
    private String reservationNo;
    private Long skuId;
    private Integer quantity;
}
