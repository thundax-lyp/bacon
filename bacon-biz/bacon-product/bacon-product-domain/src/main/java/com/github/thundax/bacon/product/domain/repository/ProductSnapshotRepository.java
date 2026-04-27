package com.github.thundax.bacon.product.domain.repository;

import com.github.thundax.bacon.product.domain.model.entity.ProductSnapshot;
import java.util.Optional;

public interface ProductSnapshotRepository {

    ProductSnapshot save(ProductSnapshot snapshot);

    Optional<ProductSnapshot> findByOrderItem(String orderNo, String orderItemNo);
}
