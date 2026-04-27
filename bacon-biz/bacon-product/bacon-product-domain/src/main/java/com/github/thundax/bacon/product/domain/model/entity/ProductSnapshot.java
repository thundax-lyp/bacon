package com.github.thundax.bacon.product.domain.model.entity;

import com.github.thundax.bacon.product.domain.exception.ProductDomainException;
import com.github.thundax.bacon.product.domain.exception.ProductErrorCode;
import java.math.BigDecimal;

public record ProductSnapshot(
        Long snapshotId,
        Long tenantId,
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
        Long productVersion) {

    public ProductSnapshot {
        requireId(snapshotId, "snapshotId");
        requireId(tenantId, "tenantId");
        requireText(orderNo, "orderNo");
        requireText(orderItemNo, "orderItemNo");
        requireId(spuId, "spuId");
        requireText(spuCode, "spuCode");
        requireText(spuName, "spuName");
        requireId(skuId, "skuId");
        requireText(skuCode, "skuCode");
        requireText(skuName, "skuName");
        requireId(categoryId, "categoryId");
        requireText(categoryName, "categoryName");
        requireText(specAttributes, "specAttributes");
        if (salePrice == null || salePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new ProductDomainException(ProductErrorCode.INVALID_SNAPSHOT, "salePrice");
        }
        if (quantity == null || quantity <= 0) {
            throw new ProductDomainException(ProductErrorCode.INVALID_SNAPSHOT, "quantity");
        }
        if (productVersion == null || productVersion <= 0) {
            throw new ProductDomainException(ProductErrorCode.INVALID_SNAPSHOT, "productVersion");
        }
    }

    public static ProductSnapshot create(
            Long snapshotId,
            String orderNo,
            String orderItemNo,
            ProductSpu spu,
            ProductSku sku,
            String categoryName,
            Integer quantity) {
        if (spu == null) {
            throw new ProductDomainException(ProductErrorCode.INVALID_SNAPSHOT, "spu");
        }
        spu.ensureCanCreateSnapshot(sku);
        return new ProductSnapshot(
                snapshotId,
                spu.getTenantId(),
                orderNo,
                orderItemNo,
                spu.getSpuId(),
                spu.getSpuCode(),
                spu.getSpuName(),
                sku.getSkuId(),
                sku.getSkuCode(),
                sku.getSkuName(),
                spu.getCategoryId(),
                categoryName,
                sku.getSpecAttributes(),
                sku.getSalePrice(),
                quantity,
                spu.getMainImageObjectId(),
                spu.getVersion());
    }

    private static void requireId(Long value, String field) {
        if (value == null || value <= 0) {
            throw new ProductDomainException(ProductErrorCode.INVALID_SNAPSHOT, field);
        }
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new ProductDomainException(ProductErrorCode.INVALID_SNAPSHOT, field);
        }
    }
}
