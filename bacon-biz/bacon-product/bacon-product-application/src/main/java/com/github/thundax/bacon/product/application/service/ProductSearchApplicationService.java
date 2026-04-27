package com.github.thundax.bacon.product.application.service;

import com.github.thundax.bacon.product.application.result.ProductSkuSaleInfoResult;
import com.github.thundax.bacon.product.domain.model.entity.ProductCategory;
import com.github.thundax.bacon.product.domain.model.entity.ProductSku;
import com.github.thundax.bacon.product.domain.model.entity.ProductSpu;
import com.github.thundax.bacon.product.domain.repository.ProductCategoryRepository;
import com.github.thundax.bacon.product.domain.repository.ProductSkuRepository;
import com.github.thundax.bacon.product.domain.repository.ProductSpuRepository;

public class ProductSearchApplicationService {

    private final ProductSpuRepository productSpuRepository;
    private final ProductSkuRepository productSkuRepository;
    private final ProductCategoryRepository productCategoryRepository;

    public ProductSearchApplicationService(
            ProductSpuRepository productSpuRepository,
            ProductSkuRepository productSkuRepository,
            ProductCategoryRepository productCategoryRepository) {
        this.productSpuRepository = productSpuRepository;
        this.productSkuRepository = productSkuRepository;
        this.productCategoryRepository = productCategoryRepository;
    }

    public boolean isSkuSaleable(Long skuId) {
        return productSkuRepository
                .findById(skuId)
                .flatMap(sku -> productSpuRepository.findById(sku.getSpuId()).map(spu -> {
                    spu.ensureCanCreateSnapshot(sku);
                    return true;
                }))
                .orElse(false);
    }

    public ProductSkuSaleInfoResult getSkuSaleInfo(Long skuId) {
        ProductSku sku = productSkuRepository.findById(skuId).orElse(null);
        if (sku == null) {
            return notSaleable(skuId, "SKU_NOT_FOUND");
        }
        ProductSpu spu = productSpuRepository.findById(sku.getSpuId()).orElse(null);
        if (spu == null) {
            return notSaleable(skuId, "PRODUCT_NOT_FOUND");
        }
        ProductCategory category = productCategoryRepository.findById(spu.getCategoryId()).orElse(null);
        boolean saleable = true;
        String failureReason = null;
        try {
            spu.ensureCanCreateSnapshot(sku);
        } catch (RuntimeException ex) {
            saleable = false;
            failureReason = ex.getMessage();
        }
        return new ProductSkuSaleInfoResult(
                spu.getTenantId(),
                spu.getSpuId(),
                spu.getSpuCode(),
                spu.getSpuName(),
                sku.getSkuId(),
                sku.getSkuCode(),
                sku.getSkuName(),
                spu.getCategoryId(),
                category == null ? null : category.getCategoryName(),
                sku.getSpecAttributes(),
                sku.getSalePrice(),
                spu.getMainImageObjectId(),
                spu.getProductStatus().value(),
                sku.getSkuStatus().value(),
                spu.getVersion(),
                saleable,
                failureReason);
    }

    private ProductSkuSaleInfoResult notSaleable(Long skuId, String failureReason) {
        return new ProductSkuSaleInfoResult(
                null, null, null, null, skuId, null, null, null, null, null, null, null, null, null, null, false,
                failureReason);
    }
}
