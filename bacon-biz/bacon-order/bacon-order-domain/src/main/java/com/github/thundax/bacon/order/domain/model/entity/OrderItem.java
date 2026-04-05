package com.github.thundax.bacon.order.domain.model.entity;

import com.github.thundax.bacon.common.core.valueobject.Money;
import com.github.thundax.bacon.common.id.domain.OrderId;
import com.github.thundax.bacon.common.id.domain.SkuId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import lombok.Getter;

/**
 * 订单项领域实体。
 */
@Getter
public class OrderItem {

    /** 所属租户主键。 */
    private final TenantId tenantId;
    /** 所属订单主键。 */
    private final OrderId orderId;
    /** 商品 SKU 主键。 */
    private final SkuId skuId;
    /** 商品 SKU 名称。 */
    private final String skuName;
    /** 商品图片快照。 */
    private final String imageUrl;
    /** 购买数量。 */
    private final Integer quantity;
    /** 销售价。 */
    private final Money salePrice;
    /** 行金额。 */
    private final Money lineAmount;

    public OrderItem(TenantId tenantId, OrderId orderId, SkuId skuId, String skuName, String imageUrl, Integer quantity,
                     Money salePrice, Money lineAmount) {
        this.tenantId = tenantId;
        this.orderId = orderId;
        this.skuId = skuId;
        this.skuName = skuName;
        this.imageUrl = imageUrl;
        this.quantity = quantity;
        this.salePrice = salePrice;
        this.lineAmount = lineAmount;
    }

    public Long getTenantIdValue() {
        return tenantId == null ? null : tenantId.value();
    }

    public Long getOrderIdValue() {
        return orderId == null ? null : orderId.value();
    }

    public Long getSkuIdValue() {
        return skuId == null ? null : skuId.value();
    }
}
