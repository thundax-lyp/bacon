package com.github.thundax.bacon.product.domain.repository;

import com.github.thundax.bacon.product.domain.model.entity.ProductImage;
import java.util.List;

public interface ProductImageRepository {

    ProductImage save(ProductImage image);

    List<ProductImage> listBySpuId(Long spuId);
}
