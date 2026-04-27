package com.github.thundax.bacon.product.infra.persistence.assembler;

import com.github.thundax.bacon.product.domain.model.entity.ProductArchive;
import com.github.thundax.bacon.product.domain.model.entity.ProductCategory;
import com.github.thundax.bacon.product.domain.model.entity.ProductIdempotencyRecord;
import com.github.thundax.bacon.product.domain.model.entity.ProductImage;
import com.github.thundax.bacon.product.domain.model.entity.ProductOutbox;
import com.github.thundax.bacon.product.domain.model.entity.ProductSku;
import com.github.thundax.bacon.product.domain.model.entity.ProductSnapshot;
import com.github.thundax.bacon.product.domain.model.entity.ProductSpu;
import com.github.thundax.bacon.product.domain.model.enums.ArchiveType;
import com.github.thundax.bacon.product.domain.model.enums.CategoryStatus;
import com.github.thundax.bacon.product.domain.model.enums.IdempotencyStatus;
import com.github.thundax.bacon.product.domain.model.enums.ImageType;
import com.github.thundax.bacon.product.domain.model.enums.OutboxEventType;
import com.github.thundax.bacon.product.domain.model.enums.OutboxStatus;
import com.github.thundax.bacon.product.domain.model.enums.ProductStatus;
import com.github.thundax.bacon.product.domain.model.enums.SkuStatus;
import com.github.thundax.bacon.product.infra.persistence.dataobject.ProductArchiveDO;
import com.github.thundax.bacon.product.infra.persistence.dataobject.ProductCategoryDO;
import com.github.thundax.bacon.product.infra.persistence.dataobject.ProductIdempotencyRecordDO;
import com.github.thundax.bacon.product.infra.persistence.dataobject.ProductImageDO;
import com.github.thundax.bacon.product.infra.persistence.dataobject.ProductOutboxDO;
import com.github.thundax.bacon.product.infra.persistence.dataobject.ProductSkuDO;
import com.github.thundax.bacon.product.infra.persistence.dataobject.ProductSnapshotDO;
import com.github.thundax.bacon.product.infra.persistence.dataobject.ProductSpuDO;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class ProductPersistenceAssembler {

    public ProductSpuDO toDataObject(ProductSpu spu, Instant now) {
        return new ProductSpuDO(
                spu.getSpuId(),
                spu.getTenantId(),
                spu.getSpuCode(),
                spu.getSpuName(),
                spu.getCategoryId(),
                spu.getDescription(),
                spu.getMainImageObjectId(),
                spu.getProductStatus().value(),
                spu.getVersion(),
                false,
                null,
                now,
                null,
                now);
    }

    public ProductSpu toSpu(ProductSpuDO dataObject) {
        return ProductSpu.reconstruct(
                dataObject.getId(),
                dataObject.getTenantId(),
                dataObject.getSpuCode(),
                dataObject.getSpuName(),
                dataObject.getCategoryId(),
                dataObject.getDescription(),
                dataObject.getMainImageObjectId(),
                ProductStatus.from(dataObject.getProductStatus()),
                dataObject.getVersion());
    }

    public ProductSkuDO toDataObject(ProductSku sku, Instant now) {
        return new ProductSkuDO(
                sku.getSkuId(),
                sku.getTenantId(),
                sku.getSpuId(),
                sku.getSkuCode(),
                sku.getSkuName(),
                sku.getSpecAttributes(),
                sku.getSalePrice(),
                sku.getSkuStatus().value(),
                false,
                null,
                now,
                null,
                now);
    }

    public ProductSku toSku(ProductSkuDO dataObject) {
        return ProductSku.reconstruct(
                dataObject.getId(),
                dataObject.getTenantId(),
                dataObject.getSpuId(),
                dataObject.getSkuCode(),
                dataObject.getSkuName(),
                dataObject.getSpecAttributes(),
                dataObject.getSalePrice(),
                SkuStatus.from(dataObject.getSkuStatus()));
    }

    public ProductCategoryDO toDataObject(ProductCategory category, Instant now) {
        return new ProductCategoryDO(
                category.getCategoryId(),
                category.getTenantId(),
                category.getParentId(),
                category.getCategoryCode(),
                category.getCategoryName(),
                category.getSortOrder(),
                category.getCategoryStatus().value(),
                false,
                null,
                now,
                null,
                now);
    }

    public ProductCategory toCategory(ProductCategoryDO dataObject) {
        return ProductCategory.reconstruct(
                dataObject.getId(),
                dataObject.getTenantId(),
                dataObject.getParentId(),
                dataObject.getCategoryCode(),
                dataObject.getCategoryName(),
                dataObject.getSortOrder(),
                CategoryStatus.from(dataObject.getCategoryStatus()));
    }

    public ProductImageDO toDataObject(ProductImage image, Instant now) {
        return new ProductImageDO(
                image.imageId(),
                image.tenantId(),
                image.spuId(),
                image.skuId(),
                image.objectId(),
                image.imageType().value(),
                image.sortOrder(),
                false,
                null,
                now,
                null,
                now);
    }

    public ProductImage toImage(ProductImageDO dataObject) {
        return ProductImage.create(
                dataObject.getId(),
                dataObject.getTenantId(),
                dataObject.getSpuId(),
                dataObject.getSkuId(),
                dataObject.getObjectId(),
                ImageType.valueOf(dataObject.getImageType()),
                dataObject.getSortOrder());
    }

    public ProductSnapshotDO toDataObject(ProductSnapshot snapshot, Instant now) {
        return new ProductSnapshotDO(
                snapshot.snapshotId(),
                snapshot.tenantId(),
                snapshot.orderNo(),
                snapshot.orderItemNo(),
                snapshot.spuId(),
                snapshot.spuCode(),
                snapshot.spuName(),
                snapshot.skuId(),
                snapshot.skuCode(),
                snapshot.skuName(),
                snapshot.categoryId(),
                snapshot.categoryName(),
                snapshot.specAttributes(),
                snapshot.salePrice(),
                snapshot.quantity(),
                snapshot.mainImageObjectId(),
                snapshot.productVersion(),
                now);
    }

    public ProductSnapshot toSnapshot(ProductSnapshotDO dataObject) {
        return new ProductSnapshot(
                dataObject.getId(),
                dataObject.getTenantId(),
                dataObject.getOrderNo(),
                dataObject.getOrderItemNo(),
                dataObject.getSpuId(),
                dataObject.getSpuCode(),
                dataObject.getSpuName(),
                dataObject.getSkuId(),
                dataObject.getSkuCode(),
                dataObject.getSkuName(),
                dataObject.getCategoryId(),
                dataObject.getCategoryName(),
                dataObject.getSpecAttributes(),
                dataObject.getSalePrice(),
                dataObject.getQuantity(),
                dataObject.getMainImageObjectId(),
                dataObject.getProductVersion());
    }

    public ProductArchiveDO toDataObject(ProductArchive archive) {
        return new ProductArchiveDO(
                archive.archiveId(),
                archive.tenantId(),
                archive.spuId(),
                archive.productVersion(),
                archive.archiveType().value(),
                archive.archiveContent(),
                archive.archivedAt());
    }

    public ProductArchive toArchive(ProductArchiveDO dataObject) {
        return new ProductArchive(
                dataObject.getId(),
                dataObject.getTenantId(),
                dataObject.getSpuId(),
                dataObject.getProductVersion(),
                ArchiveType.valueOf(dataObject.getArchiveType()),
                dataObject.getArchiveContent(),
                dataObject.getCreatedAt());
    }

    public ProductIdempotencyRecordDO toDataObject(ProductIdempotencyRecord record, Instant now) {
        return new ProductIdempotencyRecordDO(
                record.getIdempotencyId(),
                record.getTenantId(),
                record.getOperationType(),
                record.getIdempotencyKey(),
                record.getRequestHash(),
                record.getResultRefType(),
                record.getResultRefId(),
                record.getResultPayload(),
                record.getIdempotencyStatus().value(),
                null,
                now,
                now);
    }

    public ProductIdempotencyRecord toIdempotencyRecord(ProductIdempotencyRecordDO dataObject) {
        return ProductIdempotencyRecord.reconstruct(
                dataObject.getId(),
                dataObject.getTenantId(),
                dataObject.getOperationType(),
                dataObject.getIdempotencyKey(),
                dataObject.getRequestHash(),
                dataObject.getResultRefType(),
                dataObject.getResultRefId(),
                dataObject.getResultPayload(),
                IdempotencyStatus.valueOf(dataObject.getIdempotencyStatus()));
    }

    public ProductOutboxDO toDataObject(ProductOutbox outbox, Instant now) {
        return new ProductOutboxDO(
                outbox.getEventId(),
                outbox.getTenantId(),
                outbox.getAggregateId(),
                outbox.getAggregateType(),
                outbox.getEventType().value(),
                outbox.getProductVersion(),
                outbox.getPayload(),
                outbox.getOutboxStatus().value(),
                outbox.getRetryCount(),
                outbox.getNextRetryAt(),
                outbox.getProcessingOwner(),
                outbox.getLeaseUntil(),
                null,
                now,
                now);
    }

    public ProductOutbox toOutbox(ProductOutboxDO dataObject) {
        return ProductOutbox.reconstruct(
                dataObject.getId(),
                dataObject.getTenantId(),
                dataObject.getAggregateId(),
                dataObject.getAggregateType(),
                OutboxEventType.valueOf(dataObject.getEventType()),
                dataObject.getProductVersion(),
                dataObject.getPayload(),
                OutboxStatus.valueOf(dataObject.getOutboxStatus()),
                dataObject.getRetryCount(),
                dataObject.getNextRetryAt(),
                dataObject.getProcessingOwner(),
                dataObject.getLeaseUntil());
    }
}
