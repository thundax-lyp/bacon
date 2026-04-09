package com.github.thundax.bacon.order.domain.model.entity;

import com.github.thundax.bacon.common.commerce.enums.CurrencyCode;
import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import com.github.thundax.bacon.common.commerce.valueobject.Money;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderId;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单项领域实体。
 */
@Getter
@AllArgsConstructor
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

    public OrderItem(
            Long tenantId,
            Long orderId,
            Long skuId,
            String skuName,
            String imageUrl,
            Integer quantity,
            CurrencyCode currencyCode,
            String salePrice,
            String lineAmount) {
        this(
                tenantId == null ? null : TenantId.of(tenantId),
                orderId == null ? null : OrderId.of(orderId),
                skuId == null ? null : SkuId.of(skuId),
                skuName,
                imageUrl,
                quantity,
                salePrice == null ? null : Money.of(new BigDecimal(salePrice), currencyCode),
                lineAmount == null ? null : Money.of(new BigDecimal(lineAmount), currencyCode));
    }

    public Long getTenantIdValue() {
        return tenantId == null ? null : tenantId.value();
    }

    public Long getOrderIdValue() {
        return orderId == null ? null : orderId.value();
    }
}
