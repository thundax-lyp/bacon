package com.github.thundax.bacon.order.infra.repository.impl;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.order.domain.model.snapshot.OrderInventorySnapshot;
import com.github.thundax.bacon.order.domain.repository.OrderInventorySnapshotRepository;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
public class OrderInventorySnapshotRepositoryImpl implements OrderInventorySnapshotRepository {

    private final OrderInventorySnapshotRepositorySupport support;

    public OrderInventorySnapshotRepositoryImpl(OrderInventorySnapshotRepositorySupport support) {
        this.support = support;
    }

    @Override
    public void insert(OrderInventorySnapshot snapshot) {
        support.insert(snapshot);
    }

    @Override
    public void update(OrderInventorySnapshot snapshot) {
        support.update(snapshot);
    }

    @Override
    public Optional<OrderInventorySnapshot> findByOrderNo(OrderNo orderNo) {
        return support.findByOrderNo(orderNo);
    }
}
