package com.github.thundax.bacon.product.application.service;

import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.product.application.command.CreateOrderProductSnapshotCommand;
import com.github.thundax.bacon.product.application.executor.ProductIdempotencyExecutor;
import com.github.thundax.bacon.product.application.result.ProductSnapshotResult;
import com.github.thundax.bacon.product.domain.exception.ProductDomainException;
import com.github.thundax.bacon.product.domain.exception.ProductErrorCode;
import com.github.thundax.bacon.product.domain.model.entity.ProductCategory;
import com.github.thundax.bacon.product.domain.model.entity.ProductSnapshot;
import com.github.thundax.bacon.product.domain.model.entity.ProductSku;
import com.github.thundax.bacon.product.domain.model.entity.ProductSpu;
import com.github.thundax.bacon.product.domain.repository.ProductCategoryRepository;
import com.github.thundax.bacon.product.domain.repository.ProductSkuRepository;
import com.github.thundax.bacon.product.domain.repository.ProductSnapshotRepository;
import com.github.thundax.bacon.product.domain.repository.ProductSpuRepository;
import java.util.Objects;

public class ProductSnapshotApplicationService {

    private static final String SNAPSHOT_ID_BIZ_TAG = "product-snapshot-id";

    private final ProductSnapshotRepository productSnapshotRepository;
    private final ProductSpuRepository productSpuRepository;
    private final ProductSkuRepository productSkuRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductIdempotencyExecutor idempotencyExecutor;
    private final IdGenerator idGenerator;

    public ProductSnapshotApplicationService(
            ProductSnapshotRepository productSnapshotRepository,
            ProductSpuRepository productSpuRepository,
            ProductSkuRepository productSkuRepository,
            ProductCategoryRepository productCategoryRepository,
            ProductIdempotencyExecutor idempotencyExecutor,
            IdGenerator idGenerator) {
        this.productSnapshotRepository = productSnapshotRepository;
        this.productSpuRepository = productSpuRepository;
        this.productSkuRepository = productSkuRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.idempotencyExecutor = idempotencyExecutor;
        this.idGenerator = idGenerator;
    }

    public ProductSnapshotResult createOrderProductSnapshot(CreateOrderProductSnapshotCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        String idempotencyKey = command.orderNo() + ":" + command.orderItemNo();
        return idempotencyExecutor.execute(
                command.tenantId(),
                "CREATE_PRODUCT_SNAPSHOT",
                idempotencyKey,
                String.valueOf(command),
                "SNAPSHOT",
                () -> createOrderProductSnapshotOnce(command),
                result -> String.valueOf(result.snapshotId()),
                ProductSnapshotResult::toString,
                resultRefId -> toResult(productSnapshotRepository
                        .findByOrderItem(command.orderNo(), command.orderItemNo())
                        .orElseThrow(() -> new ProductDomainException(ProductErrorCode.INVALID_SNAPSHOT, resultRefId))));
    }

    private ProductSnapshotResult createOrderProductSnapshotOnce(CreateOrderProductSnapshotCommand command) {
        ProductSnapshot existing = productSnapshotRepository
                .findByOrderItem(command.orderNo(), command.orderItemNo())
                .orElse(null);
        if (existing != null) {
            return toResult(existing);
        }
        ProductSku sku = productSkuRepository
                .findById(command.skuId())
                .orElseThrow(() -> new ProductDomainException(ProductErrorCode.SKU_NOT_FOUND, String.valueOf(command.skuId())));
        ProductSpu spu = productSpuRepository
                .findById(sku.getSpuId())
                .orElseThrow(() -> new ProductDomainException(ProductErrorCode.PRODUCT_NOT_FOUND, String.valueOf(sku.getSpuId())));
        ProductCategory category = productCategoryRepository
                .findById(spu.getCategoryId())
                .orElseThrow(() -> new ProductDomainException(
                        ProductErrorCode.CATEGORY_NOT_FOUND, String.valueOf(spu.getCategoryId())));
        ProductSnapshot snapshot = ProductSnapshot.create(
                idGenerator.nextId(SNAPSHOT_ID_BIZ_TAG),
                command.orderNo(),
                command.orderItemNo(),
                spu,
                sku,
                category.getCategoryName(),
                command.quantity());
        return toResult(productSnapshotRepository.save(snapshot));
    }

    private ProductSnapshotResult toResult(ProductSnapshot snapshot) {
        return new ProductSnapshotResult(
                snapshot.tenantId(),
                snapshot.snapshotId(),
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
                snapshot.productVersion());
    }
}
