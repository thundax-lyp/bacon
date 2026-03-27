package com.github.thundax.bacon.inventory.domain.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 库存预占明细领域实体。
 */
@Getter
@AllArgsConstructor
public class InventoryReservationItem {

    /** 明细主键。 */
    private Long id;
    /** 所属租户主键。 */
    private Long tenantId;
    /** 预占单号。 */
    private String reservationNo;
    /** 商品 SKU 主键。 */
    private Long skuId;
    /** 预占数量。 */
    private Integer quantity;
}
