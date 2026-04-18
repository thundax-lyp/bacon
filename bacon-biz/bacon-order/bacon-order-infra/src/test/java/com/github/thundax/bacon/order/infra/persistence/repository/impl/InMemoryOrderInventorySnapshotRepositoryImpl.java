package com.github.thundax.bacon.order.infra.persistence.repository.impl;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.order.domain.model.snapshot.OrderInventorySnapshot;
import com.github.thundax.bacon.order.domain.repository.OrderInventorySnapshotRepository;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Primary
@Profile("test")
public class InMemoryOrderInventorySnapshotRepositoryImpl implements OrderInventorySnapshotRepository {

    private final Map<String, OrderInventorySnapshot> inventorySnapshotStorage = new ConcurrentHashMap<>();
    private final Map<String, Long> inventorySnapshotTenantStorage = new ConcurrentHashMap<>();

    @Override
    public void insert(OrderInventorySnapshot snapshot) {
        String orderNo = toOrderNoValue(snapshot.orderNo());
        inventorySnapshotStorage.put(orderNo, snapshot);
        inventorySnapshotTenantStorage.put(orderNo, currentTenantId());
    }

    @Override
    public void update(OrderInventorySnapshot snapshot) {
        String orderNo = toOrderNoValue(snapshot.orderNo());
        inventorySnapshotStorage.put(orderNo, snapshot);
        inventorySnapshotTenantStorage.put(orderNo, currentTenantId());
    }

    @Override
    public Optional<OrderInventorySnapshot> findByOrderNo(OrderNo orderNo) {
        String orderNoValue = toOrderNoValue(orderNo);
        OrderInventorySnapshot snapshot = inventorySnapshotStorage.get(orderNoValue);
        if (snapshot == null || !isTenantMatched(inventorySnapshotTenantStorage.get(orderNoValue))) {
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

    private String toOrderNoValue(OrderNo orderNo) {
        return orderNo == null ? null : orderNo.value();
    }
}
