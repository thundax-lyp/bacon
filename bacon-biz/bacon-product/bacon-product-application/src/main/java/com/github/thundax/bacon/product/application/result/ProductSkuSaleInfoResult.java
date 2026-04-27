package com.github.thundax.bacon.product.application.result;

import java.math.BigDecimal;

public record ProductSkuSaleInfoResult(
        Long tenantId,
        Long spuId,
        String spuCode,
        String spuName,
        Long skuId,
        String skuCode,
        String skuName,
        Long categoryId,
        String categoryName,
        String specAttributes,
        BigDecimal salePrice,
        String mainImageObjectId,
        String productStatus,
        String skuStatus,
        Long productVersion,
        Boolean saleable,
        String failureReason) {}
