package com.github.thundax.bacon.order.application.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    /** 商品图片快照。 */
    private String imageUrl;
    /** 购买数量。 */
    private Integer quantity;
    /** 销售价。 */
    private BigDecimal salePrice;
    /** 行金额。 */
    private BigDecimal lineAmount;
}
