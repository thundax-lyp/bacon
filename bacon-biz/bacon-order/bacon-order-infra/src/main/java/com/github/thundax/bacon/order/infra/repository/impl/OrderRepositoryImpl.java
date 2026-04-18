package com.github.thundax.bacon.order.infra.repository.impl;

import com.github.thundax.bacon.order.domain.model.entity.Order;
import com.github.thundax.bacon.order.domain.model.entity.OrderAuditLog;
import com.github.thundax.bacon.order.domain.model.entity.OrderInventorySnapshot;
import com.github.thundax.bacon.order.domain.model.entity.OrderItem;
import com.github.thundax.bacon.order.domain.model.entity.OrderPaymentSnapshot;
import com.github.thundax.bacon.order.domain.repository.OrderRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderRepositorySupport support;

    public OrderRepositoryImpl(OrderRepositorySupport support) {
        this.support = support;
    }

    @Override
    public Order insert(Order order) {
        return support.insert(order);
    }

    @Override
    public Order update(Order order) {
        return support.update(order);
    }

    @Override
    public Optional<Order> findById(Long id) {
        return support.findOrderById(id);
    }

    @Override
    public Optional<Order> findByOrderNo(String orderNo) {
        return support.findOrderByOrderNo(orderNo);
    }

    @Override
    public void updateItems(Long orderId, List<OrderItem> items) {
        support.updateItems(orderId, items);
    }

    @Override
    public List<OrderItem> listItemsByOrderId(Long orderId) {
        return support.listItemsByOrderId(orderId);
    }

    @Override
    public void insertPayment(OrderPaymentSnapshot snapshot) {
        support.insertPayment(snapshot);
    }

    @Override
    public void updatePayment(OrderPaymentSnapshot snapshot) {
        support.updatePayment(snapshot);
    }

    @Override
    public Optional<OrderPaymentSnapshot> findPaymentByOrderId(Long orderId) {
        return support.findPaymentByOrderId(orderId);
    }

    @Override
    public void insertInventory(OrderInventorySnapshot snapshot) {
        support.insertInventory(snapshot);
    }

    @Override
    public void updateInventory(OrderInventorySnapshot snapshot) {
        support.updateInventory(snapshot);
    }

    @Override
    public Optional<OrderInventorySnapshot> findInventoryByOrderNo(String orderNo) {
        return support.findInventoryByOrderNo(orderNo);
    }

    @Override
    public void insertLog(OrderAuditLog auditLog) {
        support.insertLog(auditLog);
    }

    @Override
    public List<OrderAuditLog> listLogs(String orderNo) {
        return support.listLogs(orderNo);
    }

    @Override
    public long count(
            Long userId,
            String orderNo,
            String orderStatus,
            String payStatus,
            String inventoryStatus,
            Instant createdAtFrom,
            Instant createdAtTo) {
        return support.count(
                userId, orderNo, orderStatus, payStatus, inventoryStatus, createdAtFrom, createdAtTo);
    }

    @Override
    public List<Order> page(
            Long userId,
            String orderNo,
            String orderStatus,
            String payStatus,
            String inventoryStatus,
            Instant createdAtFrom,
            Instant createdAtTo,
            int pageNo,
            int pageSize) {
        return support.page(
                userId, orderNo, orderStatus, payStatus, inventoryStatus, createdAtFrom, createdAtTo, pageNo, pageSize);
    }

    @Override
    public List<Order> list() {
        return support.list();
    }
}
