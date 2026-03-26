package com.github.thundax.bacon.order.infra.persistence.repositoryimpl;

import com.github.thundax.bacon.order.domain.model.entity.OrderIdempotencyRecord;
import com.github.thundax.bacon.order.domain.repository.OrderIdempotencyRepository;
import java.time.Instant;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnBean(OrderIdempotencyRepositorySupport.class)
public class OrderIdempotencyRepositoryImpl implements OrderIdempotencyRepository {

    private final OrderIdempotencyRepositorySupport support;

    public OrderIdempotencyRepositoryImpl(OrderIdempotencyRepositorySupport support) {
        this.support = support;
    }

    @Override
    public boolean createProcessing(OrderIdempotencyRecord record) {
        return support.createProcessing(record);
    }

    @Override
    public Optional<OrderIdempotencyRecord> findByBusinessKey(Long tenantId, String orderNo,
                                                              String paymentNo, String eventType) {
        return support.findByBusinessKey(tenantId, orderNo, paymentNo, eventType);
    }

    @Override
    public boolean markSuccess(Long tenantId, String orderNo, String paymentNo, String eventType,
                               Instant updatedAt) {
        return support.markSuccess(tenantId, orderNo, paymentNo, eventType, updatedAt);
    }

    @Override
    public boolean markFailed(Long tenantId, String orderNo, String paymentNo, String eventType,
                              String lastError, Instant updatedAt) {
        return support.markFailed(tenantId, orderNo, paymentNo, eventType, lastError, updatedAt);
    }

    @Override
    public boolean retryFromFailed(Long tenantId, String orderNo, String paymentNo, String eventType,
                                   Instant updatedAt) {
        return support.retryFromFailed(tenantId, orderNo, paymentNo, eventType, updatedAt);
    }
}
