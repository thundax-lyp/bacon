package com.github.thundax.bacon.order.domain.model.entity;

import com.github.thundax.bacon.common.commerce.enums.CurrencyCode;
import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import com.github.thundax.bacon.common.commerce.valueobject.Money;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderId;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 订单项领域实体。
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderItem {

    /** 所属订单主键。 */
    private OrderId orderId;
    /** 商品 SKU 主键。 */
    private SkuId skuId;
    /** 商品 SKU 名称。 */
    private String skuName;
    /** 商品图片快照。 */
    private String imageUrl;
    /** 购买数量。 */
    private Integer quantity;
    /** 销售价。 */
    private Money salePrice;
    /** 行金额。 */
    private Money lineAmount;

    public static OrderItem create(
            Long orderId,
            Long skuId,
            String skuName,
            String imageUrl,
            Integer quantity,
            CurrencyCode currencyCode,
            String salePrice,
            String lineAmount) {
        return new OrderItem(
                orderId == null ? null : OrderId.of(orderId),
                skuId == null ? null : SkuId.of(skuId),
                skuName,
                imageUrl,
                quantity,
                salePrice == null ? null : Money.of(new BigDecimal(salePrice), currencyCode),
                lineAmount == null ? null : Money.of(new BigDecimal(lineAmount), currencyCode));
    }

    public static OrderItem reconstruct(
            OrderId orderId,
            SkuId skuId,
            String skuName,
            String imageUrl,
            Integer quantity,
            Money salePrice,
            Money lineAmount) {
        return new OrderItem(orderId, skuId, skuName, imageUrl, quantity, salePrice, lineAmount);
    }
}
