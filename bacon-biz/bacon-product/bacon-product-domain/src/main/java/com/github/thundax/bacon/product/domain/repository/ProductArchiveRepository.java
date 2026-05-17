package com.github.thundax.bacon.product.domain.repository;

import com.github.thundax.bacon.product.domain.model.entity.ProductArchive;
import java.util.Optional;

public interface ProductArchiveRepository {

    ProductArchive insert(ProductArchive archive);

    Optional<ProductArchive> findBySpuIdAndVersion(Long spuId, Long productVersion);
}
