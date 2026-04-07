package com.github.thundax.bacon.inventory.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.WarehouseNo;
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
    private TenantId tenantId;
    /** 订单号。 */
    private String orderNo;
    /** 预占单号。 */
    private String reservationNo;
    /** 商品 SKU 主键。 */
    private Long skuId;
    /** 仓库业务编号。 */
    private WarehouseNo warehouseNo;
    /** 流水类型。 */
    private String ledgerType;
    /** 变更数量。 */
    private Integer quantity;
    /** 发生时间。 */
    private Instant occurredAt;

    public InventoryLedger(Long id, TenantId tenantId, String orderNo, String reservationNo, Long skuId,
                           String warehouseNo, String ledgerType, Integer quantity, Instant occurredAt) {
        this(id, tenantId, orderNo, reservationNo, skuId,
                warehouseNo == null ? null : WarehouseNo.of(warehouseNo),
                ledgerType, quantity, occurredAt);
    }

    public Long getTenantIdValue() {
        return tenantId == null ? null : tenantId.value();
    }

    public String getWarehouseNoValue() {
        return warehouseNo == null ? null : warehouseNo.value();
    }
}
