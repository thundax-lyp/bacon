package com.github.thundax.bacon.product.application.command;

import java.math.BigDecimal;

public record CreateProductSkuCommand(String skuCode, String skuName, String specAttributes, BigDecimal salePrice) {}
