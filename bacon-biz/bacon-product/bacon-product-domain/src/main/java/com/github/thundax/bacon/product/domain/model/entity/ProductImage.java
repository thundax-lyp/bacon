package com.github.thundax.bacon.product.domain.model.entity;

import com.github.thundax.bacon.product.domain.exception.ProductDomainException;
import com.github.thundax.bacon.product.domain.exception.ProductErrorCode;
import com.github.thundax.bacon.product.domain.model.enums.ImageType;

public record ProductImage(
        Long imageId,
        Long tenantId,
        Long spuId,
        Long skuId,
        String objectId,
        ImageType imageType,
        Integer sortOrder) {

    public ProductImage {
        requireId(imageId, "imageId");
        requireId(tenantId, "tenantId");
        requireId(spuId, "spuId");
        if (objectId == null || objectId.isBlank()) {
            throw new ProductDomainException(ProductErrorCode.INVALID_IMAGE, "objectId");
        }
        if (imageType == null) {
            throw new ProductDomainException(ProductErrorCode.INVALID_IMAGE, "imageType");
        }
        if (sortOrder == null) {
            sortOrder = 0;
        }
    }

    public static ProductImage create(
            Long imageId, Long tenantId, Long spuId, Long skuId, String objectId, ImageType imageType,
            Integer sortOrder) {
        return new ProductImage(imageId, tenantId, spuId, skuId, objectId, imageType, sortOrder);
    }

    private static void requireId(Long value, String field) {
        if (value == null || value <= 0) {
            throw new ProductDomainException(ProductErrorCode.INVALID_IMAGE, field);
        }
    }
}
