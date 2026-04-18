package com.github.thundax.bacon.order.domain.repository;

import com.github.thundax.bacon.order.domain.model.snapshot.OrderPaymentSnapshot;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderId;
import java.util.Optional;

public interface OrderPaymentSnapshotRepository {

    void insert(OrderPaymentSnapshot snapshot);

    void update(OrderPaymentSnapshot snapshot);

    Optional<OrderPaymentSnapshot> findByOrderId(OrderId orderId);
}
