package com.github.thundax.bacon.product.domain.repository;

import com.github.thundax.bacon.product.domain.model.entity.ProductOutbox;
import java.util.Optional;

public interface ProductOutboxRepository {

    ProductOutbox save(ProductOutbox outbox);

    ProductOutbox update(ProductOutbox outbox);

    Optional<ProductOutbox> findById(Long eventId);
}
