package com.github.thundax.bacon.inventory.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.SkuId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.inventory.domain.model.valueobject.ReservationNo;
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
    private TenantId tenantId;
    /** 预占单号。 */
    private ReservationNo reservationNo;
    /** 商品 SKU 主键。 */
    private SkuId skuId;
    /** 预占数量。 */
    private Integer quantity;

    public InventoryReservationItem(Long id, Long tenantId, String reservationNo, Long skuId, Integer quantity) {
        this(id,
                tenantId == null ? null : TenantId.of(tenantId),
                reservationNo == null ? null : ReservationNo.of(reservationNo),
                skuId == null ? null : SkuId.of(skuId),
                quantity);
    }

    public Long getTenantIdValue() {
        return tenantId == null ? null : tenantId.value();
    }

    public String getReservationNoValue() {
        return reservationNo == null ? null : reservationNo.value();
    }

    public Long getSkuIdValue() {
        return skuId == null ? null : skuId.value();
    }
}
