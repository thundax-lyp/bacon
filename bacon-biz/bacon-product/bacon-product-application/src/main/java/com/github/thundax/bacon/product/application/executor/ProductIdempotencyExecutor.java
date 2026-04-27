package com.github.thundax.bacon.product.application.executor;

import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.product.domain.model.entity.ProductIdempotencyRecord;
import com.github.thundax.bacon.product.domain.model.enums.IdempotencyStatus;
import com.github.thundax.bacon.product.domain.repository.ProductIdempotencyRecordRepository;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public class ProductIdempotencyExecutor {

    private static final String IDEMPOTENCY_ID_BIZ_TAG = "product-idempotency-id";

    private final ProductIdempotencyRecordRepository idempotencyRecordRepository;
    private final IdGenerator idGenerator;

    public ProductIdempotencyExecutor(
            ProductIdempotencyRecordRepository idempotencyRecordRepository, IdGenerator idGenerator) {
        this.idempotencyRecordRepository = idempotencyRecordRepository;
        this.idGenerator = idGenerator;
    }

    public <T> T execute(
            Long tenantId,
            String operationType,
            String idempotencyKey,
            String requestHash,
            String resultRefType,
            Supplier<T> action,
            Function<T, String> resultRefIdProvider,
            Function<T, String> resultPayloadProvider,
            Function<String, T> resultReader) {
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        ProductIdempotencyRecord existing = idempotencyRecordRepository
                .findByOperationTypeAndKey(operationType, idempotencyKey)
                .orElse(null);
        if (existing != null) {
            existing.ensureSameRequest(requestHash);
            if (IdempotencyStatus.SUCCESS.equals(existing.getIdempotencyStatus())) {
                return resultReader.apply(existing.getResultRefId());
            }
        }
        ProductIdempotencyRecord record = existing == null
                ? idempotencyRecordRepository.insert(ProductIdempotencyRecord.processing(
                        idGenerator.nextId(IDEMPOTENCY_ID_BIZ_TAG), tenantId, operationType, idempotencyKey, requestHash))
                : existing;
        T result = action.get();
        record.succeed(resultRefType, resultRefIdProvider.apply(result), resultPayloadProvider.apply(result));
        idempotencyRecordRepository.update(record);
        return result;
    }
}
