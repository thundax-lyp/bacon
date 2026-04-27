package com.github.thundax.bacon.product.domain.model.entity;

import com.github.thundax.bacon.product.domain.exception.ProductDomainException;
import com.github.thundax.bacon.product.domain.exception.ProductErrorCode;
import com.github.thundax.bacon.product.domain.model.enums.SkuStatus;
import java.math.BigDecimal;

public class ProductSku {

    private final Long skuId;
    private final Long tenantId;
    private final Long spuId;
    private final String skuCode;
    private String skuName;
    private String specAttributes;
    private BigDecimal salePrice;
    private SkuStatus skuStatus;

    private ProductSku(
            Long skuId,
            Long tenantId,
            Long spuId,
            String skuCode,
            String skuName,
            String specAttributes,
            BigDecimal salePrice,
            SkuStatus skuStatus) {
        this.skuId = requireId(skuId, "skuId");
        this.tenantId = requireId(tenantId, "tenantId");
        this.spuId = requireId(spuId, "spuId");
        this.skuCode = requireText(skuCode, "skuCode");
        this.skuName = requireText(skuName, "skuName");
        this.specAttributes = requireText(specAttributes, "specAttributes");
        this.salePrice = requireSalePrice(salePrice);
        this.skuStatus = requireStatus(skuStatus);
    }

    public static ProductSku create(
            Long skuId,
            Long tenantId,
            Long spuId,
            String skuCode,
            String skuName,
            String specAttributes,
            BigDecimal salePrice) {
        return new ProductSku(skuId, tenantId, spuId, skuCode, skuName, specAttributes, salePrice, SkuStatus.ENABLED);
    }

    public static ProductSku reconstruct(
            Long skuId,
            Long tenantId,
            Long spuId,
            String skuCode,
            String skuName,
            String specAttributes,
            BigDecimal salePrice,
            SkuStatus skuStatus) {
        return new ProductSku(skuId, tenantId, spuId, skuCode, skuName, specAttributes, salePrice, skuStatus);
    }

    public void updateSaleInfo(String skuName, String specAttributes, BigDecimal salePrice) {
        ensureNotDisabledOnlyByStatus();
        this.skuName = requireText(skuName, "skuName");
        this.specAttributes = requireText(specAttributes, "specAttributes");
        this.salePrice = requireSalePrice(salePrice);
    }

    public void enable() {
        this.skuStatus = SkuStatus.ENABLED;
    }

    public void disable() {
        this.skuStatus = SkuStatus.DISABLED;
    }

    public boolean isEnabled() {
        return SkuStatus.ENABLED.equals(skuStatus);
    }

    public Long getSkuId() {
        return skuId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public Long getSpuId() {
        return spuId;
    }

    public String getSkuCode() {
        return skuCode;
    }

    public String getSkuName() {
        return skuName;
    }

    public String getSpecAttributes() {
        return specAttributes;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public SkuStatus getSkuStatus() {
        return skuStatus;
    }

    private void ensureNotDisabledOnlyByStatus() {
        if (skuStatus == null) {
            throw new ProductDomainException(ProductErrorCode.INVALID_SKU_STATUS, "skuStatus");
        }
    }

    private static Long requireId(Long value, String field) {
        if (value == null || value <= 0) {
            throw new ProductDomainException(ProductErrorCode.INVALID_SKU, field);
        }
        return value;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new ProductDomainException(ProductErrorCode.INVALID_SKU, field);
        }
        return value;
    }

    private static BigDecimal requireSalePrice(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new ProductDomainException(ProductErrorCode.INVALID_SKU, "salePrice");
        }
        return value;
    }

    private static SkuStatus requireStatus(SkuStatus status) {
        if (status == null) {
            throw new ProductDomainException(ProductErrorCode.INVALID_SKU_STATUS, "skuStatus");
        }
        return status;
    }
}
