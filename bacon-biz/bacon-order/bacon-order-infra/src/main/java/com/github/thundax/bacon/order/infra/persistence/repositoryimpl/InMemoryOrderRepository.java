package com.github.thundax.bacon.order.infra.persistence.repositoryimpl;

import com.github.thundax.bacon.order.domain.model.entity.Order;
import com.github.thundax.bacon.order.domain.repository.OrderRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryOrderRepository implements OrderRepository {

    private final Map<Long, Order> storage = new ConcurrentHashMap<>();

    @Override
    public Order save(Order order) {
        storage.put(order.getId(), order);
        return order;
    }

    @Override
    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public Optional<Order> findByOrderNo(Long tenantId, String orderNo) {
        return storage.values().stream()
                .filter(order -> tenantId.equals(order.getTenantId()))
                .filter(order -> orderNo.equals(order.getOrderNo()))
                .findFirst();
    }

    @Override
    public List<Order> findAll() {
        return storage.values().stream().toList();
    }
}
