package com.github.thundax.bacon.order.domain.repository;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.order.domain.model.snapshot.OrderInventorySnapshot;
import java.util.Optional;

public interface OrderInventorySnapshotRepository {

    void insert(OrderInventorySnapshot snapshot);

    void update(OrderInventorySnapshot snapshot);

    Optional<OrderInventorySnapshot> findByOrderNo(OrderNo orderNo);
}
