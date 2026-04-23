package com.github.thundax.bacon.order.api.response;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 订单项门面响应。
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemFacadeResponse {

    private Long skuId;
    private String skuName;
    private String imageUrl;
    private Integer quantity;
    private BigDecimal salePrice;
    private BigDecimal lineAmount;
}
