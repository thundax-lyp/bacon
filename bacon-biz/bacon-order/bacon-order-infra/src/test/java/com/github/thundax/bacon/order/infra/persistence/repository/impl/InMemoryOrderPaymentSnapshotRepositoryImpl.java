package com.github.thundax.bacon.order.infra.persistence.repository.impl;

import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.order.domain.model.snapshot.OrderPaymentSnapshot;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderId;
import com.github.thundax.bacon.order.domain.repository.OrderPaymentSnapshotRepository;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Primary
@Profile("test")
public class InMemoryOrderPaymentSnapshotRepositoryImpl implements OrderPaymentSnapshotRepository {

    private final Map<Long, OrderPaymentSnapshot> paymentSnapshotStorage = new ConcurrentHashMap<>();
    private final Map<Long, Long> paymentSnapshotTenantStorage = new ConcurrentHashMap<>();

    @Override
    public void insert(OrderPaymentSnapshot snapshot) {
        Long orderId = toOrderIdValue(snapshot.orderId());
        paymentSnapshotStorage.put(orderId, snapshot);
        paymentSnapshotTenantStorage.put(orderId, currentTenantId());
    }

    @Override
    public void update(OrderPaymentSnapshot snapshot) {
        Long orderId = toOrderIdValue(snapshot.orderId());
        paymentSnapshotStorage.put(orderId, snapshot);
        paymentSnapshotTenantStorage.put(orderId, currentTenantId());
    }

    @Override
    public Optional<OrderPaymentSnapshot> findByOrderId(OrderId orderId) {
        Long orderIdValue = toOrderIdValue(orderId);
        OrderPaymentSnapshot snapshot = paymentSnapshotStorage.get(orderIdValue);
        if (snapshot == null || !isTenantMatched(paymentSnapshotTenantStorage.get(orderIdValue))) {
            return Optional.empty();
        }
        return Optional.of(snapshot);
    }

    private Long currentTenantId() {
        return BaconContextHolder.requireTenantId();
    }

    private boolean isTenantMatched(Long tenantId) {
        return tenantId != null && tenantId.equals(currentTenantId());
    }

    private Long toOrderIdValue(OrderId orderId) {
        return orderId == null ? null : orderId.value();
    }
}
