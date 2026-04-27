package com.github.thundax.bacon.product.application.document;

import com.github.thundax.bacon.product.domain.model.enums.ProductStatus;
import java.math.BigDecimal;

public record ProductSearchDocument(
        Long tenantId,
        Long spuId,
        String spuCode,
        String spuName,
        Long categoryId,
        String categoryName,
        String mainImageObjectId,
        ProductStatus productStatus,
        BigDecimal minSalePrice,
        BigDecimal maxSalePrice,
        Integer skuCount,
        Integer enabledSkuCount,
        String specSummary,
        Long productVersion) {}
