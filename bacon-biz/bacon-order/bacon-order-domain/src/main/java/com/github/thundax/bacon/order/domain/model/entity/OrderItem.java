package com.github.thundax.bacon.order.domain.model.entity;

import com.github.thundax.bacon.common.core.valueobject.Money;
import com.github.thundax.bacon.common.id.domain.OrderId;
import com.github.thundax.bacon.common.id.domain.SkuId;
import com.github.thundax.bacon.common.id.domain.TenantId;

/**
 * 订单项领域实体。
 */
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

    public TenantId getTenantId() {
        return tenantId;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public SkuId getSkuId() {
        return skuId;
    }

    public String getSkuName() {
        return skuName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public Money getSalePrice() {
        return salePrice;
    }

    public Money getLineAmount() {
        return lineAmount;
    }

    public Long getTenantIdValue() {
        return tenantId == null ? null : Long.valueOf(tenantId.value());
    }

    public Long getOrderIdValue() {
        return orderId == null ? null : Long.valueOf(orderId.value());
    }

    public Long getSkuIdValue() {
        return skuId == null ? null : skuId.value();
    }
}
