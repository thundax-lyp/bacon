package com.github.thundax.bacon.order.application.command;

import java.math.BigDecimal;

public record CreateOrderItemCommand(
        Long skuId,
        String skuName,
        String imageUrl,
        Integer quantity,
        BigDecimal salePrice
) {
}
