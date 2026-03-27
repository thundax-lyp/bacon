package com.github.thundax.bacon.inventory.api.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库存流水传输对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryLedgerDTO {

    /** 流水主键。 */
    private Long id;
    /** 所属租户主键。 */
    private Long tenantId;
    /** 订单号。 */
    private String orderNo;
    /** 预占单号。 */
    private String reservationNo;
    /** 商品 SKU 主键。 */
    private Long skuId;
    /** 仓库主键。 */
    private Long warehouseId;
    /** 流水类型。 */
    private String ledgerType;
    /** 变更数量。 */
    private Integer quantity;
    /** 发生时间。 */
    private Instant occurredAt;
}
