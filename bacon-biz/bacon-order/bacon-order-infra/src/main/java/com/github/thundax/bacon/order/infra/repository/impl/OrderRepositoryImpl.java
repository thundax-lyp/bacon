package com.github.thundax.bacon.order.infra.repository.impl;

import com.github.thundax.bacon.order.domain.model.entity.Order;
import com.github.thundax.bacon.order.domain.model.entity.OrderAuditLog;
import com.github.thundax.bacon.order.domain.model.entity.OrderInventorySnapshot;
import com.github.thundax.bacon.order.domain.model.entity.OrderItem;
import com.github.thundax.bacon.order.domain.model.entity.OrderPaymentSnapshot;
import com.github.thundax.bacon.order.domain.repository.OrderRepository;
import com.github.thundax.bacon.order.infra.persistence.repository.impl.OrderRepositorySupport;
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
    public Order save(Order order) {
        return support.saveOrder(order);
    }

    @Override
    public Optional<Order> findById(Long id) {
        return support.findOrderById(id);
    }

    @Override
    public Optional<Order> findByOrderNo(Long tenantId, String orderNo) {
        return support.findOrderByOrderNo(tenantId, orderNo);
    }

    @Override
    public void saveItems(Long tenantId, Long orderId, List<OrderItem> items) {
        support.saveItems(tenantId, orderId, items);
    }

    @Override
    public List<OrderItem> findItemsByOrderId(Long tenantId, Long orderId) {
        return support.findItemsByOrderId(tenantId, orderId);
    }

    @Override
    public void savePaymentSnapshot(OrderPaymentSnapshot snapshot) {
        support.savePaymentSnapshot(snapshot);
    }

    @Override
    public Optional<OrderPaymentSnapshot> findPaymentSnapshotByOrderId(Long tenantId, Long orderId) {
        return support.findPaymentSnapshotByOrderId(tenantId, orderId);
    }

    @Override
    public void saveInventorySnapshot(OrderInventorySnapshot snapshot) {
        support.saveInventorySnapshot(snapshot);
    }

    @Override
    public Optional<OrderInventorySnapshot> findInventorySnapshotByOrderNo(Long tenantId, String orderNo) {
        return support.findInventorySnapshotByOrderNo(tenantId, orderNo);
    }

    @Override
    public void saveAuditLog(OrderAuditLog auditLog) {
        support.saveAuditLog(auditLog);
    }

    @Override
    public List<OrderAuditLog> findAuditLogs(Long tenantId, String orderNo) {
        return support.findAuditLogs(tenantId, orderNo);
    }

    @Override
    public long countOrders(Long tenantId, Long userId, String orderNo, String orderStatus, String payStatus,
                            String inventoryStatus, Instant createdAtFrom, Instant createdAtTo) {
        return support.countOrders(tenantId, userId, orderNo, orderStatus, payStatus, inventoryStatus,
                createdAtFrom, createdAtTo);
    }

    @Override
    public List<Order> pageOrders(Long tenantId, Long userId, String orderNo, String orderStatus, String payStatus,
                                  String inventoryStatus, Instant createdAtFrom, Instant createdAtTo,
                                  int offset, int limit) {
        return support.pageOrders(tenantId, userId, orderNo, orderStatus, payStatus, inventoryStatus,
                createdAtFrom, createdAtTo, offset, limit);
    }

    @Override
    public List<Order> findAll() {
        return support.findAll();
    }
}
