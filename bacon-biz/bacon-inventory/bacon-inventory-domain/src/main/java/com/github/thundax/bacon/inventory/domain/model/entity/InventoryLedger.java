package com.github.thundax.bacon.inventory.domain.model.entity;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 库存流水领域实体。
 */
@Getter
@AllArgsConstructor
public class InventoryLedger {

    public static final String TYPE_RESERVE = "RESERVE";
    public static final String TYPE_RELEASE = "RELEASE";
    public static final String TYPE_DEDUCT = "DEDUCT";

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
