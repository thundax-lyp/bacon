package com.github.thundax.bacon.inventory.interfaces.response;

import com.github.thundax.bacon.inventory.application.dto.InventoryLedgerDTO;
import java.time.Instant;

/**
 * 库存流水响应对象。
 */
public record InventoryLedgerResponse(
        /** 流水主键。 */
        Long id,
        /** 订单号。 */
        String orderNo,
        /** 预占单号。 */
        String reservationNo,
        /** 商品 SKU 主键。 */
        Long skuId,
        /** 仓库业务编码。 */
        String warehouseCode,
        /** 流水类型。 */
        String ledgerType,
        /** 变更数量。 */
        Integer quantity,
        /** 发生时间。 */
        Instant occurredAt) {

    public static InventoryLedgerResponse from(InventoryLedgerDTO dto) {
        return new InventoryLedgerResponse(
                dto.getId(),
                dto.getOrderNo(),
                dto.getReservationNo(),
                dto.getSkuId(),
                dto.getWarehouseCode(),
                dto.getLedgerType(),
                dto.getQuantity(),
                dto.getOccurredAt());
    }
}
