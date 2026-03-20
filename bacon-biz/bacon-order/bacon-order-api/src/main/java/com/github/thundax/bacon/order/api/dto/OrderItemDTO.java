package com.github.thundax.bacon.order.api.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {

    private Long skuId;
    private String skuName;
    private Integer quantity;
    private BigDecimal salePrice;
    private BigDecimal lineAmount;
}
