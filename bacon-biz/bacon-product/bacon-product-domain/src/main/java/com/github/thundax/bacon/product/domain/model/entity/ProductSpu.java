package com.github.thundax.bacon.product.domain.model.entity;

import com.github.thundax.bacon.product.domain.exception.ProductDomainException;
import com.github.thundax.bacon.product.domain.exception.ProductErrorCode;
import com.github.thundax.bacon.product.domain.model.enums.ProductStatus;
import java.util.List;

public class ProductSpu {

    private static final long INITIAL_VERSION = 1L;

    private final Long spuId;
    private final Long tenantId;
    private final String spuCode;
    private String spuName;
    private Long categoryId;
    private String description;
    private String mainImageObjectId;
    private ProductStatus productStatus;
    private Long version;

    private ProductSpu(
            Long spuId,
            Long tenantId,
            String spuCode,
            String spuName,
            Long categoryId,
            String description,
            String mainImageObjectId,
            ProductStatus productStatus,
            Long version) {
        this.spuId = requireId(spuId, "spuId");
        this.tenantId = requireId(tenantId, "tenantId");
        this.spuCode = requireText(spuCode, "spuCode");
        this.spuName = requireText(spuName, "spuName");
        this.categoryId = requireId(categoryId, "categoryId");
        this.description = description;
        this.mainImageObjectId = mainImageObjectId;
        this.productStatus = requireStatus(productStatus);
        this.version = requireVersion(version);
    }

    public static ProductSpu create(
            Long spuId,
            Long tenantId,
            String spuCode,
            String spuName,
            Long categoryId,
            String description,
            String mainImageObjectId) {
        return new ProductSpu(
                spuId, tenantId, spuCode, spuName, categoryId, description, mainImageObjectId, ProductStatus.DRAFT,
                INITIAL_VERSION);
    }

    public static ProductSpu reconstruct(
            Long spuId,
            Long tenantId,
            String spuCode,
            String spuName,
            Long categoryId,
            String description,
            String mainImageObjectId,
            ProductStatus productStatus,
            Long version) {
        return new ProductSpu(
                spuId, tenantId, spuCode, spuName, categoryId, description, mainImageObjectId, productStatus, version);
    }

    public void updateBase(
            String spuName, Long categoryId, String description, String mainImageObjectId, Long expectedVersion) {
        ensureEditable();
        ensureExpectedVersion(expectedVersion);
        this.spuName = requireText(spuName, "spuName");
        this.categoryId = requireId(categoryId, "categoryId");
        this.description = description;
        this.mainImageObjectId = mainImageObjectId;
        increaseVersion();
    }

    public void markSkuChanged(Long expectedVersion) {
        ensureEditable();
        ensureExpectedVersion(expectedVersion);
        increaseVersion();
    }

    public void onSale(List<ProductSku> skus, Long expectedVersion) {
        ensureEditable();
        ensureExpectedVersion(expectedVersion);
        if (skus == null || skus.stream().noneMatch(ProductSku::isEnabled)) {
            throw new ProductDomainException(ProductErrorCode.INVALID_PRODUCT_STATUS, "enabled sku is required");
        }
        this.productStatus = ProductStatus.ON_SALE;
        increaseVersion();
    }

    public void offSale(Long expectedVersion) {
        ensureExpectedVersion(expectedVersion);
        ensureNotArchived();
        this.productStatus = ProductStatus.OFF_SALE;
        increaseVersion();
    }

    public void archive(Long expectedVersion) {
        ensureExpectedVersion(expectedVersion);
        ensureNotArchived();
        this.productStatus = ProductStatus.ARCHIVED;
        increaseVersion();
    }

    public void ensureCanCreateSnapshot(ProductSku sku) {
        if (!ProductStatus.ON_SALE.equals(productStatus)) {
            throw new ProductDomainException(ProductErrorCode.INVALID_PRODUCT_STATUS, productStatus.value());
        }
        if (sku == null || !sku.isEnabled()) {
            throw new ProductDomainException(ProductErrorCode.INVALID_SKU_STATUS, "sku not saleable");
        }
        if (!spuId.equals(sku.getSpuId()) || !tenantId.equals(sku.getTenantId())) {
            throw new ProductDomainException(ProductErrorCode.INVALID_SKU, "sku does not belong to product");
        }
    }

    public Long getSpuId() {
        return spuId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public String getSpuCode() {
        return spuCode;
    }

    public String getSpuName() {
        return spuName;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getDescription() {
        return description;
    }

    public String getMainImageObjectId() {
        return mainImageObjectId;
    }

    public ProductStatus getProductStatus() {
        return productStatus;
    }

    public Long getVersion() {
        return version;
    }

    private void ensureEditable() {
        ensureNotArchived();
    }

    private void ensureNotArchived() {
        if (ProductStatus.ARCHIVED.equals(productStatus)) {
            throw new ProductDomainException(ProductErrorCode.INVALID_PRODUCT_STATUS, "archived");
        }
    }

    private void ensureExpectedVersion(Long expectedVersion) {
        if (expectedVersion == null || !version.equals(expectedVersion)) {
            throw new ProductDomainException(ProductErrorCode.VERSION_CONFLICT, String.valueOf(expectedVersion));
        }
    }

    private void increaseVersion() {
        this.version = this.version + 1;
    }

    private static Long requireId(Long value, String field) {
        if (value == null || value <= 0) {
            throw new ProductDomainException(ProductErrorCode.INVALID_PRODUCT, field);
        }
        return value;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new ProductDomainException(ProductErrorCode.INVALID_PRODUCT, field);
        }
        return value;
    }

    private static ProductStatus requireStatus(ProductStatus status) {
        if (status == null) {
            throw new ProductDomainException(ProductErrorCode.INVALID_PRODUCT_STATUS, "productStatus");
        }
        return status;
    }

    private static Long requireVersion(Long version) {
        if (version == null || version < INITIAL_VERSION) {
            throw new ProductDomainException(ProductErrorCode.INVALID_PRODUCT, "version");
        }
        return version;
    }
}
