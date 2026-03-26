package com.github.thundax.bacon.order.domain.model.entity;

import java.math.BigDecimal;

public class OrderItem {

    private final Long tenantId;
    private final Long orderId;
    private final Long skuId;
    private final String skuName;
    private final Integer quantity;
    private final BigDecimal salePrice;
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
