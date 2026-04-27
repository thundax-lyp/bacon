package com.github.thundax.bacon.product.application.service;

import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.product.application.command.ChangeProductStatusCommand;
import com.github.thundax.bacon.product.application.command.CreateProductCommand;
import com.github.thundax.bacon.product.application.command.CreateProductSkuCommand;
import com.github.thundax.bacon.product.application.command.UpdateProductCommand;
import com.github.thundax.bacon.product.application.executor.ProductIdempotencyExecutor;
import com.github.thundax.bacon.product.application.result.ProductCommandResult;
import com.github.thundax.bacon.product.domain.exception.ProductDomainException;
import com.github.thundax.bacon.product.domain.exception.ProductErrorCode;
import com.github.thundax.bacon.product.domain.model.entity.ProductCategory;
import com.github.thundax.bacon.product.domain.model.entity.ProductOutbox;
import com.github.thundax.bacon.product.domain.model.entity.ProductSku;
import com.github.thundax.bacon.product.domain.model.entity.ProductSpu;
import com.github.thundax.bacon.product.domain.model.enums.OutboxEventType;
import com.github.thundax.bacon.product.domain.repository.ProductCategoryRepository;
import com.github.thundax.bacon.product.domain.repository.ProductOutboxRepository;
import com.github.thundax.bacon.product.domain.repository.ProductSkuRepository;
import com.github.thundax.bacon.product.domain.repository.ProductSpuRepository;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ProductManagementApplicationService {

    private static final String PRODUCT_ID_BIZ_TAG = "product-spu-id";
    private static final String PRODUCT_SKU_ID_BIZ_TAG = "product-sku-id";
    private static final String PRODUCT_OUTBOX_ID_BIZ_TAG = "product-outbox-id";

    private final ProductSpuRepository productSpuRepository;
    private final ProductSkuRepository productSkuRepository;
    private final ProductCategoryRepository productCategoryRepository;
    private final ProductOutboxRepository productOutboxRepository;
    private final ProductIdempotencyExecutor idempotencyExecutor;
    private final IdGenerator idGenerator;

    public ProductManagementApplicationService(
            ProductSpuRepository productSpuRepository,
            ProductSkuRepository productSkuRepository,
            ProductCategoryRepository productCategoryRepository,
            ProductOutboxRepository productOutboxRepository,
            ProductIdempotencyExecutor idempotencyExecutor,
            IdGenerator idGenerator) {
        this.productSpuRepository = productSpuRepository;
        this.productSkuRepository = productSkuRepository;
        this.productCategoryRepository = productCategoryRepository;
        this.productOutboxRepository = productOutboxRepository;
        this.idempotencyExecutor = idempotencyExecutor;
        this.idGenerator = idGenerator;
    }

    public ProductCommandResult createProduct(CreateProductCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        return idempotencyExecutor.execute(
                command.tenantId(),
                "CREATE_PRODUCT",
                command.idempotencyKey(),
                String.valueOf(command),
                "PRODUCT",
                () -> createProductOnce(command),
                result -> String.valueOf(result.spuId()),
                ProductCommandResult::toString,
                resultRefId -> toResult(productSpuRepository
                        .findById(Long.valueOf(resultRefId))
                        .orElseThrow(() -> new ProductDomainException(ProductErrorCode.PRODUCT_NOT_FOUND, resultRefId))));
    }

    public ProductCommandResult updateProduct(UpdateProductCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        return idempotencyExecutor.execute(
                command.tenantId(),
                "UPDATE_PRODUCT",
                command.idempotencyKey(),
                String.valueOf(command),
                "PRODUCT",
                () -> updateProductOnce(command),
                result -> String.valueOf(result.spuId()),
                ProductCommandResult::toString,
                resultRefId -> toResult(productSpuRepository
                        .findById(Long.valueOf(resultRefId))
                        .orElseThrow(() -> new ProductDomainException(ProductErrorCode.PRODUCT_NOT_FOUND, resultRefId))));
    }

    public ProductCommandResult onSale(ChangeProductStatusCommand command) {
        ProductSpu spu = loadSpu(command.spuId());
        List<ProductSku> skus = productSkuRepository.listBySpuId(command.spuId());
        spu.onSale(skus, command.expectedVersion());
        ProductSpu saved = productSpuRepository.update(spu);
        saveOutbox(saved, OutboxEventType.PRODUCT_STATUS_CHANGED);
        return toResult(saved);
    }

    public ProductCommandResult offSale(ChangeProductStatusCommand command) {
        ProductSpu spu = loadSpu(command.spuId());
        spu.offSale(command.expectedVersion());
        ProductSpu saved = productSpuRepository.update(spu);
        saveOutbox(saved, OutboxEventType.PRODUCT_STATUS_CHANGED);
        return toResult(saved);
    }

    public ProductCommandResult archive(ChangeProductStatusCommand command) {
        ProductSpu spu = loadSpu(command.spuId());
        spu.archive(command.expectedVersion());
        ProductSpu saved = productSpuRepository.update(spu);
        saveOutbox(saved, OutboxEventType.PRODUCT_ARCHIVED);
        return toResult(saved);
    }

    private ProductCommandResult createProductOnce(CreateProductCommand command) {
        ensureCategoryEnabled(command.categoryId());
        ProductSpu spu = ProductSpu.create(
                idGenerator.nextId(PRODUCT_ID_BIZ_TAG),
                command.tenantId(),
                command.spuCode(),
                command.spuName(),
                command.categoryId(),
                command.description(),
                command.mainImageObjectId());
        ProductSpu saved = productSpuRepository.save(spu);
        normalizeSkus(command.skus()).forEach(skuCommand -> productSkuRepository.save(ProductSku.create(
                idGenerator.nextId(PRODUCT_SKU_ID_BIZ_TAG),
                command.tenantId(),
                saved.getSpuId(),
                skuCommand.skuCode(),
                skuCommand.skuName(),
                skuCommand.specAttributes(),
                skuCommand.salePrice())));
        saveOutbox(saved, OutboxEventType.PRODUCT_CREATED);
        return toResult(saved);
    }

    private ProductCommandResult updateProductOnce(UpdateProductCommand command) {
        ensureCategoryEnabled(command.categoryId());
        ProductSpu spu = loadSpu(command.spuId());
        spu.updateBase(
                command.spuName(),
                command.categoryId(),
                command.description(),
                command.mainImageObjectId(),
                command.expectedVersion());
        ProductSpu saved = productSpuRepository.update(spu);
        saveOutbox(saved, OutboxEventType.PRODUCT_UPDATED);
        return toResult(saved);
    }

    private void ensureCategoryEnabled(Long categoryId) {
        ProductCategory category = productCategoryRepository
                .findById(categoryId)
                .orElseThrow(() -> new ProductDomainException(ProductErrorCode.CATEGORY_NOT_FOUND, String.valueOf(categoryId)));
        if (!category.isEnabled()) {
            throw new ProductDomainException(ProductErrorCode.INVALID_CATEGORY, "category disabled");
        }
    }

    private ProductSpu loadSpu(Long spuId) {
        return productSpuRepository
                .findById(spuId)
                .orElseThrow(() -> new ProductDomainException(ProductErrorCode.PRODUCT_NOT_FOUND, String.valueOf(spuId)));
    }

    private void saveOutbox(ProductSpu spu, OutboxEventType eventType) {
        productOutboxRepository.save(ProductOutbox.create(
                idGenerator.nextId(PRODUCT_OUTBOX_ID_BIZ_TAG), spu, eventType,
                "{\"spuId\":" + spu.getSpuId() + ",\"productVersion\":" + spu.getVersion() + "}"));
    }

    private ProductCommandResult toResult(ProductSpu spu) {
        return new ProductCommandResult(
                spu.getTenantId(), spu.getSpuId(), spu.getSpuCode(), spu.getProductStatus(), spu.getVersion());
    }

    private List<CreateProductSkuCommand> normalizeSkus(List<CreateProductSkuCommand> skus) {
        if (skus == null || skus.isEmpty()) {
            throw new ProductDomainException(ProductErrorCode.INVALID_SKU, "sku is required");
        }
        return skus.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }
}
