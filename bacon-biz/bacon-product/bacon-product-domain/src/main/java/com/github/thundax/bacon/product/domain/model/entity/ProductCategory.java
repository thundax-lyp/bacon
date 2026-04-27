package com.github.thundax.bacon.product.domain.model.entity;

import com.github.thundax.bacon.product.domain.exception.ProductDomainException;
import com.github.thundax.bacon.product.domain.exception.ProductErrorCode;
import com.github.thundax.bacon.product.domain.model.enums.CategoryStatus;

public class ProductCategory {

    private final Long categoryId;
    private final Long tenantId;
    private final Long parentId;
    private final String categoryCode;
    private String categoryName;
    private Integer sortOrder;
    private CategoryStatus categoryStatus;

    private ProductCategory(
            Long categoryId,
            Long tenantId,
            Long parentId,
            String categoryCode,
            String categoryName,
            Integer sortOrder,
            CategoryStatus categoryStatus) {
        this.categoryId = requireId(categoryId, "categoryId");
        this.tenantId = requireId(tenantId, "tenantId");
        this.parentId = parentId;
        this.categoryCode = requireText(categoryCode, "categoryCode");
        this.categoryName = requireText(categoryName, "categoryName");
        this.sortOrder = sortOrder == null ? 0 : sortOrder;
        this.categoryStatus = requireStatus(categoryStatus);
    }

    public static ProductCategory create(
            Long categoryId, Long tenantId, Long parentId, String categoryCode, String categoryName, Integer sortOrder) {
        return new ProductCategory(
                categoryId, tenantId, parentId, categoryCode, categoryName, sortOrder, CategoryStatus.ENABLED);
    }

    public static ProductCategory reconstruct(
            Long categoryId,
            Long tenantId,
            Long parentId,
            String categoryCode,
            String categoryName,
            Integer sortOrder,
            CategoryStatus categoryStatus) {
        return new ProductCategory(
                categoryId, tenantId, parentId, categoryCode, categoryName, sortOrder, categoryStatus);
    }

    public void update(String categoryName, Integer sortOrder) {
        this.categoryName = requireText(categoryName, "categoryName");
        this.sortOrder = sortOrder == null ? 0 : sortOrder;
    }

    public void enable() {
        this.categoryStatus = CategoryStatus.ENABLED;
    }

    public void disable() {
        this.categoryStatus = CategoryStatus.DISABLED;
    }

    public boolean isEnabled() {
        return CategoryStatus.ENABLED.equals(categoryStatus);
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public Long getParentId() {
        return parentId;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public CategoryStatus getCategoryStatus() {
        return categoryStatus;
    }

    private static Long requireId(Long value, String field) {
        if (value == null || value <= 0) {
            throw new ProductDomainException(ProductErrorCode.INVALID_CATEGORY, field);
        }
        return value;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new ProductDomainException(ProductErrorCode.INVALID_CATEGORY, field);
        }
        return value;
    }

    private static CategoryStatus requireStatus(CategoryStatus status) {
        if (status == null) {
            throw new ProductDomainException(ProductErrorCode.INVALID_CATEGORY, "categoryStatus");
        }
        return status;
    }
}
