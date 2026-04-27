package com.github.thundax.bacon.product.application.service;

import com.github.thundax.bacon.product.domain.repository.ProductSkuRepository;
import com.github.thundax.bacon.product.domain.repository.ProductSpuRepository;

public class ProductSearchApplicationService {

    private final ProductSpuRepository productSpuRepository;
    private final ProductSkuRepository productSkuRepository;

    public ProductSearchApplicationService(ProductSpuRepository productSpuRepository, ProductSkuRepository productSkuRepository) {
        this.productSpuRepository = productSpuRepository;
        this.productSkuRepository = productSkuRepository;
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
}
