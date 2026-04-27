package com.github.thundax.bacon.product.interfaces.response;

import java.math.BigDecimal;

public record ProductSnapshotResponse(
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
