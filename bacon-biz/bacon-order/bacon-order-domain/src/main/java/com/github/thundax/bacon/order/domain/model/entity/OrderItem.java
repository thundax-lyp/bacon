package com.github.thundax.bacon.order.domain.model.entity;

import com.github.thundax.bacon.common.commerce.enums.CurrencyCode;
import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import com.github.thundax.bacon.common.commerce.util.MoneyValidator;
import com.github.thundax.bacon.common.commerce.valueobject.Money;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderId;
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

    /** 订单项主键。 */
    private Long id;
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
            Long id,
            Long orderId,
            Long skuId,
            String skuName,
            String imageUrl,
            Integer quantity,
            Money salePrice,
            Money lineAmount) {
        MoneyValidator.ensureSameCurrency(salePrice, lineAmount);
        return new OrderItem(
                id,
                orderId == null ? null : OrderId.of(orderId),
                skuId == null ? null : SkuId.of(skuId),
                skuName,
                imageUrl,
                quantity,
                salePrice,
                lineAmount);
    }

    public static OrderItem reconstruct(
            Long id,
            OrderId orderId,
            SkuId skuId,
            String skuName,
            String imageUrl,
            Integer quantity,
            Money salePrice,
            Money lineAmount) {
        MoneyValidator.ensureSameCurrency(salePrice, lineAmount);
        return new OrderItem(id, orderId, skuId, skuName, imageUrl, quantity, salePrice, lineAmount);
    }
}
