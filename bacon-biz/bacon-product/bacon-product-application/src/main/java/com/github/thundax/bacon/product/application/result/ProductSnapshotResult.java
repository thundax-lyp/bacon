package com.github.thundax.bacon.product.application.result;

import java.math.BigDecimal;

public record ProductSnapshotResult(
        Long tenantId,
        Long snapshotId,
        String orderNo,
        String orderItemNo,
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
        Integer quantity,
        String mainImageObjectId,
        Long productVersion) {}
