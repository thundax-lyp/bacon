package com.github.thundax.bacon.product.infra.search;

import com.github.thundax.bacon.product.application.document.ProductSearchDocument;
import java.math.BigDecimal;
import java.time.Instant;

public record ProductSearchDocumentPayload(
        Long tenantId,
        Long spuId,
        String spuCode,
        String spuName,
        Long categoryId,
        String categoryName,
        String mainImageObjectId,
        String productStatus,
        BigDecimal minSalePrice,
        BigDecimal maxSalePrice,
        Integer skuCount,
        Integer enabledSkuCount,
        String specSummary,
        String searchText,
        Long productVersion,
        Instant indexedAt) {

    public static ProductSearchDocumentPayload from(ProductSearchDocument document) {
        String searchText = String.join(
                " ",
                document.spuName() == null ? "" : document.spuName(),
                document.categoryName() == null ? "" : document.categoryName(),
                document.specSummary() == null ? "" : document.specSummary());
        return new ProductSearchDocumentPayload(
                document.tenantId(),
                document.spuId(),
                document.spuCode(),
                document.spuName(),
                document.categoryId(),
                document.categoryName(),
                document.mainImageObjectId(),
                document.productStatus() == null ? null : document.productStatus().value(),
                document.minSalePrice(),
                document.maxSalePrice(),
                document.skuCount(),
                document.enabledSkuCount(),
                document.specSummary(),
                searchText,
                document.productVersion(),
                Instant.now());
    }
}
