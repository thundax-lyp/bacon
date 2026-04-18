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
    public Order insertOrder(Order order) {
        return support.insertOrder(order);
    }

    @Override
    public Order updateOrder(Order order) {
        return support.updateOrder(order);
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
    public List<OrderItem> findItemsByOrderId(Long orderId) {
        return support.findItemsByOrderId(orderId);
    }

    @Override
    public void insertPaymentSnapshot(OrderPaymentSnapshot snapshot) {
        support.insertPaymentSnapshot(snapshot);
    }

    @Override
    public void updatePaymentSnapshot(OrderPaymentSnapshot snapshot) {
        support.updatePaymentSnapshot(snapshot);
    }

    @Override
    public Optional<OrderPaymentSnapshot> findPaymentSnapshotByOrderId(Long orderId) {
        return support.findPaymentSnapshotByOrderId(orderId);
    }

    @Override
    public void insertInventorySnapshot(OrderInventorySnapshot snapshot) {
        support.insertInventorySnapshot(snapshot);
    }

    @Override
    public void updateInventorySnapshot(OrderInventorySnapshot snapshot) {
        support.updateInventorySnapshot(snapshot);
    }

    @Override
    public Optional<OrderInventorySnapshot> findInventorySnapshotByOrderNo(String orderNo) {
        return support.findInventorySnapshotByOrderNo(orderNo);
    }

    @Override
    public void insertAuditLog(OrderAuditLog auditLog) {
        support.insertAuditLog(auditLog);
    }

    @Override
    public List<OrderAuditLog> findAuditLogs(String orderNo) {
        return support.findAuditLogs(orderNo);
    }

    @Override
    public long countOrders(
            Long userId,
            String orderNo,
            String orderStatus,
            String payStatus,
            String inventoryStatus,
            Instant createdAtFrom,
            Instant createdAtTo) {
        return support.countOrders(
                userId, orderNo, orderStatus, payStatus, inventoryStatus, createdAtFrom, createdAtTo);
    }

    @Override
    public List<Order> pageOrders(
            Long userId,
            String orderNo,
            String orderStatus,
            String payStatus,
            String inventoryStatus,
            Instant createdAtFrom,
            Instant createdAtTo,
            int pageNo,
            int pageSize) {
        return support.pageOrders(
                userId, orderNo, orderStatus, payStatus, inventoryStatus, createdAtFrom, createdAtTo, pageNo, pageSize);
    }

    @Override
    public List<Order> findAll() {
        return support.findAll();
    }
}
