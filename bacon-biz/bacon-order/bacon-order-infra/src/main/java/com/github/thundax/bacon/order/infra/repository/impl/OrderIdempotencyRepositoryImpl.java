package com.github.thundax.bacon.order.infra.repository.impl;

import com.github.thundax.bacon.order.domain.model.entity.OrderIdempotencyRecord;
import com.github.thundax.bacon.order.domain.model.enums.OrderIdempotencyStatus;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderIdempotencyRecordKey;
import com.github.thundax.bacon.order.domain.repository.OrderIdempotencyRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
public class OrderIdempotencyRepositoryImpl implements OrderIdempotencyRepository {

    private final OrderIdempotencyRepositorySupport support;

    public OrderIdempotencyRepositoryImpl(OrderIdempotencyRepositorySupport support) {
        this.support = support;
    }

    @Override
    public boolean insert(OrderIdempotencyRecord record) {
        return support.insert(record);
    }

    @Override
    public boolean updateStatus(OrderIdempotencyRecord record, OrderIdempotencyStatus currentStatus) {
        return support.updateStatus(record, currentStatus);
    }

    @Override
    public Optional<OrderIdempotencyRecord> findByKey(OrderIdempotencyRecordKey key) {
        return support.findByKey(key);
    }

    @Override
    public boolean updateStatus(
            OrderIdempotencyRecord record,
            OrderIdempotencyStatus currentStatus,
            Instant leaseExpiredBefore) {
        return support.updateStatus(record, currentStatus, leaseExpiredBefore);
    }

    @Override
    public List<OrderIdempotencyRecord> listExpiredProcessing(Instant now) {
        return support.listExpiredProcessing(now);
    }
}
