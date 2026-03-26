package com.github.thundax.bacon.order.domain.repository;

import com.github.thundax.bacon.order.domain.model.entity.OrderIdempotencyRecord;
import java.time.Instant;
import java.util.Optional;

public interface OrderIdempotencyRepository {

    default boolean createProcessing(OrderIdempotencyRecord record) {
        return false;
    }

    default boolean claimExpiredProcessing(Long tenantId, String orderNo, String paymentNo, String eventType,
                                           String processingOwner, Instant leaseUntil, Instant claimedAt,
                                           Instant updatedAt) {
        return false;
    }

    default Optional<OrderIdempotencyRecord> findByBusinessKey(Long tenantId, String orderNo,
                                                                String paymentNo, String eventType) {
        return Optional.empty();
    }

    default boolean markSuccess(Long tenantId, String orderNo, String paymentNo, String eventType,
                                Instant updatedAt) {
        return false;
    }

    default boolean markFailed(Long tenantId, String orderNo, String paymentNo, String eventType,
                               String lastError, Instant updatedAt) {
        return false;
    }

    default boolean retryFromFailed(Long tenantId, String orderNo, String paymentNo, String eventType,
                                    Instant updatedAt) {
        return false;
    }

    default boolean retryFromFailed(Long tenantId, String orderNo, String paymentNo, String eventType,
                                    String processingOwner, Instant leaseUntil, Instant claimedAt, Instant updatedAt) {
        return false;
    }

    default int recoverExpiredProcessing(Instant now, String recoverMessage) {
        return 0;
    }
}
