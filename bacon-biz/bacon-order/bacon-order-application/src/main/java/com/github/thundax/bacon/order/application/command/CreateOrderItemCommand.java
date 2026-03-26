package com.github.thundax.bacon.order.application.command;

import java.math.BigDecimal;

public record CreateOrderItemCommand(
        Long skuId,
        String skuName,
        Integer quantity,
        BigDecimal salePrice
) {
}
