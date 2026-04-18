package com.github.thundax.bacon.order.infra.repository.impl;

import com.github.thundax.bacon.order.domain.model.snapshot.OrderPaymentSnapshot;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderId;
import com.github.thundax.bacon.order.domain.repository.OrderPaymentSnapshotRepository;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
public class OrderPaymentSnapshotRepositoryImpl implements OrderPaymentSnapshotRepository {

    private final OrderPaymentSnapshotRepositorySupport support;

    public OrderPaymentSnapshotRepositoryImpl(OrderPaymentSnapshotRepositorySupport support) {
        this.support = support;
    }

    @Override
    public void insert(OrderPaymentSnapshot snapshot) {
        support.insert(snapshot);
    }

    @Override
    public void update(OrderPaymentSnapshot snapshot) {
        support.update(snapshot);
    }

    @Override
    public Optional<OrderPaymentSnapshot> findByOrderId(OrderId orderId) {
        return support.findByOrderId(orderId);
    }
}
