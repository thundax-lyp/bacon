package com.github.thundax.bacon.product.api.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductSnapshotDTO(
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
        Long productVersion,
        Instant createdAt) {}
