package com.github.thundax.bacon.product.domain.repository;

import com.github.thundax.bacon.product.domain.model.entity.ProductSku;
import java.util.List;
import java.util.Optional;

public interface ProductSkuRepository {

    ProductSku save(ProductSku sku);

    ProductSku update(ProductSku sku);

    Optional<ProductSku> findById(Long skuId);

    Optional<ProductSku> findByCode(String skuCode);

    List<ProductSku> listBySpuId(Long spuId);
}
