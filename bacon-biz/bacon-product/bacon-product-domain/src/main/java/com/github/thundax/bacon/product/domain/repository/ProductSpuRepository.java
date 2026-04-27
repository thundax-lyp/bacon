package com.github.thundax.bacon.product.domain.repository;

import com.github.thundax.bacon.product.domain.model.entity.ProductSpu;
import java.util.Optional;

public interface ProductSpuRepository {

    ProductSpu save(ProductSpu spu);

    ProductSpu update(ProductSpu spu);

    Optional<ProductSpu> findById(Long spuId);

    Optional<ProductSpu> findByCode(String spuCode);
}
