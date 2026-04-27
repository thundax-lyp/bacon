package com.github.thundax.bacon.product.domain.repository;

import com.github.thundax.bacon.product.domain.model.entity.ProductIdempotencyRecord;
import java.util.Optional;

public interface ProductIdempotencyRecordRepository {

    ProductIdempotencyRecord insert(ProductIdempotencyRecord record);

    ProductIdempotencyRecord update(ProductIdempotencyRecord record);

    Optional<ProductIdempotencyRecord> findByOperationTypeAndKey(String operationType, String idempotencyKey);
}
