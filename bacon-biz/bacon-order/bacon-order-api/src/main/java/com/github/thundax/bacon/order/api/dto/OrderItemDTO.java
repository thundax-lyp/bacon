package com.github.thundax.bacon.order.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 订单项传输对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {

    /** 商品 SKU 主键。 */
    private Long skuId;
    /** 商品 SKU 名称。 */
    private String skuName;
    /** 购买数量。 */
    private Integer quantity;
    /** 销售价。 */
    private BigDecimal salePrice;
    /** 行金额。 */
    private BigDecimal lineAmount;
}
