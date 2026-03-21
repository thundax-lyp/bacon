package com.github.thundax.bacon.order.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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
