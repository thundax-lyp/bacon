package com.github.thundax.bacon.product.domain.model.entity;

import com.github.thundax.bacon.product.domain.exception.ProductDomainException;
import com.github.thundax.bacon.product.domain.exception.ProductErrorCode;
import com.github.thundax.bacon.product.domain.model.enums.ArchiveType;
import java.time.Instant;

public record ProductArchive(
        Long archiveId,
        Long tenantId,
        Long spuId,
        Long productVersion,
        ArchiveType archiveType,
        String archiveContent,
        Instant archivedAt) {

    public ProductArchive {
        requireId(archiveId, "archiveId");
        requireId(tenantId, "tenantId");
        requireId(spuId, "spuId");
        requireId(productVersion, "productVersion");
        if (archiveType == null) {
            throw new ProductDomainException(ProductErrorCode.INVALID_ARCHIVE, "archiveType");
        }
        if (archiveContent == null || archiveContent.isBlank()) {
            throw new ProductDomainException(ProductErrorCode.INVALID_ARCHIVE, "archiveContent");
        }
        if (archivedAt == null) {
            throw new ProductDomainException(ProductErrorCode.INVALID_ARCHIVE, "archivedAt");
        }
    }

    public static ProductArchive create(
            Long archiveId, ProductSpu spu, ArchiveType archiveType, String archiveContent, Instant archivedAt) {
        if (spu == null) {
            throw new ProductDomainException(ProductErrorCode.INVALID_ARCHIVE, "spu");
        }
        return new ProductArchive(
                archiveId, spu.getTenantId(), spu.getSpuId(), spu.getVersion(), archiveType, archiveContent,
                archivedAt);
    }

    private static void requireId(Long value, String field) {
        if (value == null || value <= 0) {
            throw new ProductDomainException(ProductErrorCode.INVALID_ARCHIVE, field);
        }
    }
}
