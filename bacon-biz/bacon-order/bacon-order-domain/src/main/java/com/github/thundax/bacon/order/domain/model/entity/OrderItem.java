package com.github.thundax.bacon.order.domain.model.entity;

import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import com.github.thundax.bacon.common.commerce.util.MoneyValidator;
import com.github.thundax.bacon.common.commerce.valueobject.Money;
import com.github.thundax.bacon.order.domain.exception.OrderDomainException;
import com.github.thundax.bacon.order.domain.exception.OrderErrorCode;
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
            OrderId orderId,
            SkuId skuId,
            String skuName,
            String imageUrl,
            Integer quantity,
            Money salePrice) {
        ensureSkuId(skuId);
        ensureQuantity(quantity);
        ensureSalePrice(salePrice);
        return new OrderItem(id, orderId, skuId, skuName, imageUrl, quantity, salePrice, salePrice.multiply(quantity));
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
        ensureSkuId(skuId);
        ensureQuantity(quantity);
        ensureSalePrice(salePrice);
        ensureLineAmount(lineAmount);
        MoneyValidator.ensureSameCurrency(salePrice, lineAmount);
        ensureLineAmountMatches(quantity, salePrice, lineAmount);
        return new OrderItem(id, orderId, skuId, skuName, imageUrl, quantity, salePrice, lineAmount);
    }

    private static void ensureSkuId(SkuId skuId) {
        if (skuId == null) {
            throw new OrderDomainException(OrderErrorCode.INVALID_ORDER_ITEM, "skuId is required");
        }
    }

    private static void ensureQuantity(Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new OrderDomainException(OrderErrorCode.INVALID_ORDER_ITEM, "quantity must be greater than 0");
        }
    }

    private static void ensureSalePrice(Money salePrice) {
        if (salePrice == null) {
            throw new OrderDomainException(OrderErrorCode.INVALID_ORDER_ITEM, "salePrice is required");
        }
        if (salePrice.value().compareTo(BigDecimal.ZERO) < 0) {
            throw new OrderDomainException(OrderErrorCode.INVALID_ORDER_ITEM, "salePrice must not be negative");
        }
    }

    private static void ensureLineAmount(Money lineAmount) {
        if (lineAmount == null) {
            throw new OrderDomainException(OrderErrorCode.INVALID_ORDER_ITEM, "lineAmount is required");
        }
        if (lineAmount.value().compareTo(BigDecimal.ZERO) < 0) {
            throw new OrderDomainException(OrderErrorCode.INVALID_ORDER_ITEM, "lineAmount must not be negative");
        }
    }

    private static void ensureLineAmountMatches(Integer quantity, Money salePrice, Money lineAmount) {
        if (salePrice.multiply(quantity).equals(lineAmount)) {
            return;
        }
        throw new OrderDomainException(OrderErrorCode.INVALID_ORDER_ITEM, "lineAmount does not match quantity * salePrice");
    }
}
