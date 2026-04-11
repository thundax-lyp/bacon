package com.github.thundax.bacon.inventory.domain.model.entity;

import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.commerce.valueobject.WarehouseCode;
import com.github.thundax.bacon.inventory.domain.model.enums.InventoryLedgerType;
import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservationNo;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 库存流水领域实体。
 */
@Getter
@AllArgsConstructor
public class InventoryLedger {

    /** 流水主键。 */
    private Long id;
    /** 订单号。 */
    private OrderNo orderNo;
    /** 预占单号。 */
    private ReservationNo reservationNo;
    /** 商品 SKU 主键。 */
    private SkuId skuId;
    /** 仓库业务编码。 */
    private WarehouseCode warehouseCode;
    /** 流水类型。 */
    private InventoryLedgerType ledgerType;
    /** 变更数量。 */
    private Integer quantity;
    /** 发生时间。 */
    private Instant occurredAt;
}
