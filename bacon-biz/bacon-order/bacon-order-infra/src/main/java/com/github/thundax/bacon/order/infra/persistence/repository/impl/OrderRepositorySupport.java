package com.github.thundax.bacon.order.infra.persistence.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.order.domain.model.entity.Order;
import com.github.thundax.bacon.order.domain.model.entity.OrderAuditLog;
import com.github.thundax.bacon.order.domain.model.entity.OrderInventorySnapshot;
import com.github.thundax.bacon.order.domain.model.entity.OrderItem;
import com.github.thundax.bacon.order.domain.model.entity.OrderPaymentSnapshot;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderPageQuery;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderPageResult;
import com.github.thundax.bacon.order.infra.persistence.dataobject.OrderAuditLogDataObject;
import com.github.thundax.bacon.order.infra.persistence.dataobject.OrderDataObject;
import com.github.thundax.bacon.order.infra.persistence.dataobject.OrderInventorySnapshotDataObject;
import com.github.thundax.bacon.order.infra.persistence.dataobject.OrderItemDataObject;
import com.github.thundax.bacon.order.infra.persistence.dataobject.OrderPaymentSnapshotDataObject;
import com.github.thundax.bacon.order.infra.persistence.mapper.OrderAuditLogMapper;
import com.github.thundax.bacon.order.infra.persistence.mapper.OrderInventorySnapshotMapper;
import com.github.thundax.bacon.order.infra.persistence.mapper.OrderItemMapper;
import com.github.thundax.bacon.order.infra.persistence.mapper.OrderMapper;
import com.github.thundax.bacon.order.infra.persistence.mapper.OrderPaymentSnapshotMapper;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderRepositorySupport {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderPaymentSnapshotMapper orderPaymentSnapshotMapper;
    private final OrderInventorySnapshotMapper orderInventorySnapshotMapper;
    private final OrderAuditLogMapper orderAuditLogMapper;

    public OrderRepositorySupport(OrderMapper orderMapper,
                                  OrderItemMapper orderItemMapper,
                                  OrderPaymentSnapshotMapper orderPaymentSnapshotMapper,
                                  OrderInventorySnapshotMapper orderInventorySnapshotMapper,
                                  OrderAuditLogMapper orderAuditLogMapper) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.orderPaymentSnapshotMapper = orderPaymentSnapshotMapper;
        this.orderInventorySnapshotMapper = orderInventorySnapshotMapper;
        this.orderAuditLogMapper = orderAuditLogMapper;
        log.info("Using MyBatis-Plus order repository");
    }

    public Order saveOrder(Order order) {
        OrderDataObject dataObject = toDataObject(order);
        dataObject.setUpdatedAt(Instant.now());
        if (dataObject.getId() == null) {
            orderMapper.insert(dataObject);
            order.setId(dataObject.getId());
        } else {
            // 订单主表只承载主单核心字段；支付和库存侧派生信息不直接塞回主表，而是走快照表分开维护。
            orderMapper.updateById(dataObject);
        }
        return order;
    }

    public Optional<Order> findOrderById(Long id) {
        return Optional.ofNullable(orderMapper.selectById(id)).map(this::toDomainWithSnapshots);
    }

    public Optional<Order> findOrderByOrderNo(Long tenantId, String orderNo) {
        return Optional.ofNullable(orderMapper.selectOne(Wrappers.<OrderDataObject>lambdaQuery()
                .eq(OrderDataObject::getTenantId, tenantId)
                .eq(OrderDataObject::getOrderNo, orderNo)))
                .map(this::toDomainWithSnapshots);
    }

    public void saveItems(Long tenantId, Long orderId, List<OrderItem> items) {
        // 订单项采用“先删后插”的整包替换策略，保持应用层传入的 items 列表就是该订单的权威快照。
        orderItemMapper.delete(Wrappers.<OrderItemDataObject>lambdaQuery()
                .eq(OrderItemDataObject::getTenantId, tenantId)
                .eq(OrderItemDataObject::getOrderId, orderId));
        if (items == null || items.isEmpty()) {
            return;
        }
        for (OrderItem item : items) {
            orderItemMapper.insert(new OrderItemDataObject(null, item.getTenantId(), item.getOrderId(), item.getSkuId(),
                    item.getSkuName(), item.getQuantity(), item.getSalePrice(), item.getLineAmount()));
        }
    }

    public List<OrderItem> findItemsByOrderId(Long tenantId, Long orderId) {
        return orderItemMapper.selectList(Wrappers.<OrderItemDataObject>lambdaQuery()
                        .eq(OrderItemDataObject::getTenantId, tenantId)
                        .eq(OrderItemDataObject::getOrderId, orderId)
                        .orderByAsc(OrderItemDataObject::getId))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    public void savePaymentSnapshot(OrderPaymentSnapshot snapshot) {
        OrderPaymentSnapshotDataObject existing = orderPaymentSnapshotMapper.selectOne(
                Wrappers.<OrderPaymentSnapshotDataObject>lambdaQuery()
                        .eq(OrderPaymentSnapshotDataObject::getTenantId, snapshot.tenantId())
                        .eq(OrderPaymentSnapshotDataObject::getOrderId, snapshot.orderId()));
        OrderPaymentSnapshotDataObject dataObject = toDataObject(snapshot);
        dataObject.setUpdatedAt(snapshot.updatedAt() == null ? Instant.now() : snapshot.updatedAt());
        // 支付快照按 orderId 唯一覆盖，目标是保留“当前支付视图”，而不是积累每次变化历史。
        if (existing == null) {
            orderPaymentSnapshotMapper.insert(dataObject);
            return;
        }
        dataObject.setId(existing.getId());
        orderPaymentSnapshotMapper.updateById(dataObject);
    }

    public Optional<OrderPaymentSnapshot> findPaymentSnapshotByOrderId(Long tenantId, Long orderId) {
        return Optional.ofNullable(orderPaymentSnapshotMapper.selectOne(
                Wrappers.<OrderPaymentSnapshotDataObject>lambdaQuery()
                        .eq(OrderPaymentSnapshotDataObject::getTenantId, tenantId)
                        .eq(OrderPaymentSnapshotDataObject::getOrderId, orderId)))
                .map(this::toDomain);
    }

    public void saveInventorySnapshot(OrderInventorySnapshot snapshot) {
        OrderInventorySnapshotDataObject existing = orderInventorySnapshotMapper.selectOne(
                Wrappers.<OrderInventorySnapshotDataObject>lambdaQuery()
                        .eq(OrderInventorySnapshotDataObject::getTenantId, snapshot.tenantId())
                        .eq(OrderInventorySnapshotDataObject::getOrderId, snapshot.orderId()));
        OrderInventorySnapshotDataObject dataObject = toDataObject(snapshot);
        dataObject.setUpdatedAt(snapshot.updatedAt() == null ? Instant.now() : snapshot.updatedAt());
        // 库存快照和支付快照一样采用唯一覆盖模型，分页/详情查询只需要当前库存派生状态。
        if (existing == null) {
            orderInventorySnapshotMapper.insert(dataObject);
            return;
        }
        dataObject.setId(existing.getId());
        orderInventorySnapshotMapper.updateById(dataObject);
    }

    public Optional<OrderInventorySnapshot> findInventorySnapshotByOrderId(Long tenantId, Long orderId) {
        return Optional.ofNullable(orderInventorySnapshotMapper.selectOne(
                Wrappers.<OrderInventorySnapshotDataObject>lambdaQuery()
                        .eq(OrderInventorySnapshotDataObject::getTenantId, tenantId)
                        .eq(OrderInventorySnapshotDataObject::getOrderId, orderId)))
                .map(this::toDomain);
    }

    public void saveAuditLog(OrderAuditLog auditLog) {
        orderAuditLogMapper.insert(toDataObject(auditLog));
    }

    public List<OrderAuditLog> findAuditLogs(Long tenantId, String orderNo) {
        return orderAuditLogMapper.selectList(Wrappers.<OrderAuditLogDataObject>lambdaQuery()
                        .eq(OrderAuditLogDataObject::getTenantId, tenantId)
                        .eq(OrderAuditLogDataObject::getOrderNo, orderNo)
                        .orderByAsc(OrderAuditLogDataObject::getOccurredAt, OrderAuditLogDataObject::getId))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    public OrderPageResult pageOrders(OrderPageQuery query) {
        long total = Optional.ofNullable(orderMapper.selectCount(buildPageQuery(query))).orElse(0L);
        if (total <= 0) {
            return new OrderPageResult(List.of(), 0L);
        }
        List<OrderDataObject> pageOrders = orderMapper.selectList(buildPageQuery(query)
                .orderByDesc(OrderDataObject::getCreatedAt, OrderDataObject::getId)
                .last("limit " + query.offset() + "," + query.limit()));
        if (pageOrders.isEmpty()) {
            return new OrderPageResult(List.of(), total);
        }
        List<Long> orderIds = pageOrders.stream()
                .map(OrderDataObject::getId)
                .toList();
        // 分页查询先批量拉主单，再一次性批量拉支付/库存快照，避免逐单 N+1 查询。
        Map<Long, OrderPaymentSnapshotDataObject> paymentSnapshotMap = orderPaymentSnapshotMapper.selectList(
                        Wrappers.<OrderPaymentSnapshotDataObject>lambdaQuery()
                                .in(OrderPaymentSnapshotDataObject::getOrderId, orderIds))
                .stream()
                .collect(Collectors.toMap(OrderPaymentSnapshotDataObject::getOrderId, Function.identity(),
                        (left, right) -> left));
        Map<Long, OrderInventorySnapshotDataObject> inventorySnapshotMap = orderInventorySnapshotMapper.selectList(
                        Wrappers.<OrderInventorySnapshotDataObject>lambdaQuery()
                                .in(OrderInventorySnapshotDataObject::getOrderId, orderIds))
                .stream()
                .collect(Collectors.toMap(OrderInventorySnapshotDataObject::getOrderId, Function.identity(),
                        (left, right) -> left));
        List<Order> records = pageOrders.stream()
                .map(orderData -> toDomain(orderData, paymentSnapshotMap.get(orderData.getId()),
                        inventorySnapshotMap.get(orderData.getId())))
                .toList();
        return new OrderPageResult(records, total);
    }

    public List<Order> findAll() {
        return orderMapper.selectList(Wrappers.<OrderDataObject>lambdaQuery()
                        .orderByDesc(OrderDataObject::getCreatedAt, OrderDataObject::getId))
                .stream()
                .map(this::toDomainWithSnapshots)
                .toList();
    }

    private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OrderDataObject> buildPageQuery(
            OrderPageQuery query) {
        return Wrappers.<OrderDataObject>lambdaQuery()
                .eq(query.tenantId() != null, OrderDataObject::getTenantId, query.tenantId())
                .eq(query.userId() != null, OrderDataObject::getUserId, query.userId())
                .like(query.orderNo() != null && !query.orderNo().isBlank(), OrderDataObject::getOrderNo, query.orderNo())
                .eq(query.orderStatus() != null && !query.orderStatus().isBlank(),
                        OrderDataObject::getOrderStatus, query.orderStatus())
                .eq(query.payStatus() != null && !query.payStatus().isBlank(), OrderDataObject::getPayStatus, query.payStatus())
                .eq(query.inventoryStatus() != null && !query.inventoryStatus().isBlank(),
                        OrderDataObject::getInventoryStatus, query.inventoryStatus())
                .ge(query.createdAtFrom() != null, OrderDataObject::getCreatedAt, query.createdAtFrom())
                .le(query.createdAtTo() != null, OrderDataObject::getCreatedAt, query.createdAtTo());
    }

    private OrderDataObject toDataObject(Order order) {
        return new OrderDataObject(order.getId(), order.getTenantId(), order.getOrderNo(), order.getUserId(),
                order.getOrderStatus(), order.getPayStatus(), order.getInventoryStatus(), order.getCurrencyCode(),
                order.getTotalAmount(), order.getPayableAmount(), order.getRemark(), order.getCancelReason(),
                order.getCloseReason(), order.getCreatedAt(), Instant.now(), order.getExpiredAt(), order.getPaidAt(),
                order.getClosedAt());
    }

    private Order toDomainWithSnapshots(OrderDataObject dataObject) {
        // 详情查询需要把主表和两张快照表重新拼成完整领域对象，保证应用层看到的是统一视图。
        OrderPaymentSnapshotDataObject paymentSnapshot = orderPaymentSnapshotMapper.selectOne(
                Wrappers.<OrderPaymentSnapshotDataObject>lambdaQuery()
                        .eq(OrderPaymentSnapshotDataObject::getTenantId, dataObject.getTenantId())
                        .eq(OrderPaymentSnapshotDataObject::getOrderId, dataObject.getId()));
        OrderInventorySnapshotDataObject inventorySnapshot = orderInventorySnapshotMapper.selectOne(
                Wrappers.<OrderInventorySnapshotDataObject>lambdaQuery()
                        .eq(OrderInventorySnapshotDataObject::getTenantId, dataObject.getTenantId())
                        .eq(OrderInventorySnapshotDataObject::getOrderId, dataObject.getId()));
        return toDomain(dataObject, paymentSnapshot, inventorySnapshot);
    }

    private Order toDomain(OrderDataObject dataObject, OrderPaymentSnapshotDataObject paymentSnapshot,
                           OrderInventorySnapshotDataObject inventorySnapshot) {
        // rehydrate 时优先用快照表补回 paymentNo/reservationNo 等派生字段，
        // 因为这些字段在 strict 持久化模型里并不全部固化在订单主表。
        return Order.rehydrate(dataObject.getId(), dataObject.getTenantId(), dataObject.getOrderNo(),
                dataObject.getUserId(), dataObject.getOrderStatus(), dataObject.getPayStatus(),
                dataObject.getInventoryStatus(),
                paymentSnapshot == null ? null : paymentSnapshot.getPaymentNo(),
                inventorySnapshot == null ? null : inventorySnapshot.getReservationNo(),
                dataObject.getCurrencyCode(), dataObject.getTotalAmount(),
                dataObject.getPayableAmount(), dataObject.getRemark(), dataObject.getCancelReason(),
                dataObject.getCloseReason(), dataObject.getCreatedAt(), dataObject.getExpiredAt(),
                dataObject.getPaidAt(), dataObject.getClosedAt(),
                paymentSnapshot == null ? null : paymentSnapshot.getChannelCode(),
                paymentSnapshot == null ? null : paymentSnapshot.getPaidAmount(),
                paymentSnapshot == null ? null : paymentSnapshot.getChannelStatus(),
                paymentSnapshot == null ? null : paymentSnapshot.getFailureReason(),
                null,
                inventorySnapshot == null ? null : inventorySnapshot.getWarehouseId(),
                inventorySnapshot == null ? null : inventorySnapshot.getFailureReason(),
                null, null, null);
    }

    private OrderItem toDomain(OrderItemDataObject dataObject) {
        return new OrderItem(dataObject.getTenantId(), dataObject.getOrderId(), dataObject.getSkuId(),
                dataObject.getSkuName(), dataObject.getQuantity(), dataObject.getSalePrice(), dataObject.getLineAmount());
    }

    private OrderPaymentSnapshotDataObject toDataObject(OrderPaymentSnapshot snapshot) {
        return new OrderPaymentSnapshotDataObject(snapshot.id(), snapshot.tenantId(), snapshot.orderId(),
                snapshot.paymentNo(), snapshot.channelCode(), snapshot.payStatus(), snapshot.paidAmount(),
                snapshot.paidTime(), snapshot.failureReason(), snapshot.channelStatus(), snapshot.updatedAt());
    }

    private OrderPaymentSnapshot toDomain(OrderPaymentSnapshotDataObject dataObject) {
        return new OrderPaymentSnapshot(dataObject.getId(), dataObject.getTenantId(), dataObject.getOrderId(),
                dataObject.getPaymentNo(), dataObject.getChannelCode(), dataObject.getPayStatus(),
                dataObject.getPaidAmount(), dataObject.getPaidTime(), dataObject.getFailureReason(),
                dataObject.getChannelStatus(), dataObject.getUpdatedAt());
    }

    private OrderInventorySnapshotDataObject toDataObject(OrderInventorySnapshot snapshot) {
        return new OrderInventorySnapshotDataObject(snapshot.id(), snapshot.tenantId(), snapshot.orderId(),
                snapshot.reservationNo(), snapshot.inventoryStatus(), snapshot.warehouseId(), snapshot.failureReason(),
                snapshot.updatedAt());
    }

    private OrderInventorySnapshot toDomain(OrderInventorySnapshotDataObject dataObject) {
        return new OrderInventorySnapshot(dataObject.getId(), dataObject.getTenantId(), dataObject.getOrderId(),
                dataObject.getReservationNo(), dataObject.getInventoryStatus(), dataObject.getWarehouseId(),
                dataObject.getFailureReason(), dataObject.getUpdatedAt());
    }

    private OrderAuditLogDataObject toDataObject(OrderAuditLog auditLog) {
        return new OrderAuditLogDataObject(auditLog.id(), auditLog.tenantId(), auditLog.orderNo(), auditLog.actionType(),
                auditLog.beforeStatus(), auditLog.afterStatus(), auditLog.operatorType(), auditLog.operatorId(),
                auditLog.occurredAt());
    }

    private OrderAuditLog toDomain(OrderAuditLogDataObject dataObject) {
        return new OrderAuditLog(dataObject.getId(), dataObject.getTenantId(), dataObject.getOrderNo(),
                dataObject.getActionType(), dataObject.getBeforeStatus(), dataObject.getAfterStatus(),
                dataObject.getOperatorType(), dataObject.getOperatorId(), dataObject.getOccurredAt());
    }
}
