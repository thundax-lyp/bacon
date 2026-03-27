package com.github.thundax.bacon.order.domain.model.entity;

import java.math.BigDecimal;

/**
 * 订单项领域实体。
 */
public class OrderItem {

    /** 所属租户主键。 */
    private final Long tenantId;
    /** 所属订单主键。 */
    private final Long orderId;
    /** 商品 SKU 主键。 */
    private final Long skuId;
    /** 商品 SKU 名称。 */
    private final String skuName;
    /** 购买数量。 */
    private final Integer quantity;
    /** 销售价。 */
    private final BigDecimal salePrice;
    /** 行金额。 */
    private final BigDecimal lineAmount;

    public OrderItem(Long tenantId, Long orderId, Long skuId, String skuName, Integer quantity, BigDecimal salePrice,
                     BigDecimal lineAmount) {
        this.tenantId = tenantId;
        this.orderId = orderId;
        this.skuId = skuId;
        this.skuName = skuName;
        this.quantity = quantity;
        this.salePrice = salePrice;
        this.lineAmount = lineAmount;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getSkuId() {
        return skuId;
    }

    public String getSkuName() {
        return skuName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public BigDecimal getLineAmount() {
        return lineAmount;
    }
}
