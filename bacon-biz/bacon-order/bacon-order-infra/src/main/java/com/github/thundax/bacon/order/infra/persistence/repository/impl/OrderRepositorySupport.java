package com.github.thundax.bacon.order.infra.persistence.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.core.enums.CurrencyCode;
import com.github.thundax.bacon.common.core.valueobject.Money;
import com.github.thundax.bacon.common.id.domain.OrderId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.order.domain.model.entity.Order;
import com.github.thundax.bacon.order.domain.model.entity.OrderAuditLog;
import com.github.thundax.bacon.order.domain.model.entity.OrderInventorySnapshot;
import com.github.thundax.bacon.order.domain.model.entity.OrderItem;
import com.github.thundax.bacon.order.domain.model.entity.OrderPaymentSnapshot;
import com.github.thundax.bacon.order.domain.model.entity.OrderStatus;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderPageQuery;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderPageResult;
import com.github.thundax.bacon.order.infra.persistence.dataobject.OrderAuditLogDO;
import com.github.thundax.bacon.order.infra.persistence.dataobject.OrderDO;
import com.github.thundax.bacon.order.infra.persistence.dataobject.OrderInventorySnapshotDO;
import com.github.thundax.bacon.order.infra.persistence.dataobject.OrderItemDO;
import com.github.thundax.bacon.order.infra.persistence.dataobject.OrderPaymentSnapshotDO;
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
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!test")
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
        OrderDO dataObject = toDataObject(order);
        dataObject.setUpdatedAt(Instant.now());
        if (dataObject.getId() == null) {
            orderMapper.insert(dataObject);
            order.setId(toDomainOrderId(dataObject.getId()));
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
        return Optional.ofNullable(orderMapper.selectOne(Wrappers.<OrderDO>lambdaQuery()
                .eq(OrderDO::getTenantId, tenantId)
                .eq(OrderDO::getOrderNo, orderNo)))
                .map(this::toDomainWithSnapshots);
    }

    public void saveItems(Long tenantId, Long orderId, List<OrderItem> items) {
        // 订单项采用“先删后插”的整包替换策略，保持应用层传入的 items 列表就是该订单的权威快照。
        orderItemMapper.delete(Wrappers.<OrderItemDO>lambdaQuery()
                .eq(OrderItemDO::getTenantId, tenantId)
                .eq(OrderItemDO::getOrderId, orderId));
        if (items == null || items.isEmpty()) {
            return;
        }
        for (OrderItem item : items) {
            orderItemMapper.insert(new OrderItemDO(null, item.getTenantId(), item.getOrderId(), item.getSkuId(),
                    item.getSkuName(), item.getQuantity(), item.getSalePrice(), item.getLineAmount()));
        }
    }

    public List<OrderItem> findItemsByOrderId(Long tenantId, Long orderId) {
        return orderItemMapper.selectList(Wrappers.<OrderItemDO>lambdaQuery()
                        .eq(OrderItemDO::getTenantId, tenantId)
                        .eq(OrderItemDO::getOrderId, orderId)
                        .orderByAsc(OrderItemDO::getId))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    public void savePaymentSnapshot(OrderPaymentSnapshot snapshot) {
        OrderPaymentSnapshotDO existing = orderPaymentSnapshotMapper.selectOne(
                Wrappers.<OrderPaymentSnapshotDO>lambdaQuery()
                        .eq(OrderPaymentSnapshotDO::getTenantId, snapshot.tenantId())
                        .eq(OrderPaymentSnapshotDO::getOrderId, snapshot.orderId()));
        OrderPaymentSnapshotDO dataObject = toDataObject(snapshot);
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
                Wrappers.<OrderPaymentSnapshotDO>lambdaQuery()
                        .eq(OrderPaymentSnapshotDO::getTenantId, tenantId)
                        .eq(OrderPaymentSnapshotDO::getOrderId, orderId)))
                .map(this::toDomain);
    }

    public void saveInventorySnapshot(OrderInventorySnapshot snapshot) {
        OrderInventorySnapshotDO existing = orderInventorySnapshotMapper.selectOne(
                Wrappers.<OrderInventorySnapshotDO>lambdaQuery()
                        .eq(OrderInventorySnapshotDO::getTenantId, snapshot.tenantId())
                        .eq(OrderInventorySnapshotDO::getOrderId, snapshot.orderId()));
        OrderInventorySnapshotDO dataObject = toDataObject(snapshot);
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
                Wrappers.<OrderInventorySnapshotDO>lambdaQuery()
                        .eq(OrderInventorySnapshotDO::getTenantId, tenantId)
                        .eq(OrderInventorySnapshotDO::getOrderId, orderId)))
                .map(this::toDomain);
    }

    public void saveAuditLog(OrderAuditLog auditLog) {
        orderAuditLogMapper.insert(toDataObject(auditLog));
    }

    public List<OrderAuditLog> findAuditLogs(Long tenantId, String orderNo) {
        return orderAuditLogMapper.selectList(Wrappers.<OrderAuditLogDO>lambdaQuery()
                        .eq(OrderAuditLogDO::getTenantId, tenantId)
                        .eq(OrderAuditLogDO::getOrderNo, orderNo)
                        .orderByAsc(OrderAuditLogDO::getOccurredAt, OrderAuditLogDO::getId))
                .stream()
                .map(this::toDomain)
                .toList();
    }

    public OrderPageResult pageOrders(OrderPageQuery query) {
        long total = Optional.ofNullable(orderMapper.selectCount(buildPageQuery(query))).orElse(0L);
        if (total <= 0) {
            return new OrderPageResult(List.of(), 0L);
        }
        List<OrderDO> pageOrders = orderMapper.selectList(buildPageQuery(query)
                .orderByDesc(OrderDO::getCreatedAt, OrderDO::getId)
                .last("limit " + query.offset() + "," + query.limit()));
        if (pageOrders.isEmpty()) {
            return new OrderPageResult(List.of(), total);
        }
        List<Long> orderIds = pageOrders.stream()
                .map(OrderDO::getId)
                .toList();
        // 分页查询先批量拉主单，再一次性批量拉支付/库存快照，避免逐单 N+1 查询。
        Map<Long, OrderPaymentSnapshotDO> paymentSnapshotMap = orderPaymentSnapshotMapper.selectList(
                        Wrappers.<OrderPaymentSnapshotDO>lambdaQuery()
                                .in(OrderPaymentSnapshotDO::getOrderId, orderIds))
                .stream()
                .collect(Collectors.toMap(OrderPaymentSnapshotDO::getOrderId, Function.identity(),
                        (left, right) -> left));
        Map<Long, OrderInventorySnapshotDO> inventorySnapshotMap = orderInventorySnapshotMapper.selectList(
                        Wrappers.<OrderInventorySnapshotDO>lambdaQuery()
                                .in(OrderInventorySnapshotDO::getOrderId, orderIds))
                .stream()
                .collect(Collectors.toMap(OrderInventorySnapshotDO::getOrderId, Function.identity(),
                        (left, right) -> left));
        List<Order> records = pageOrders.stream()
                .map(orderData -> toDomain(orderData, paymentSnapshotMap.get(orderData.getId()),
                        inventorySnapshotMap.get(orderData.getId())))
                .toList();
        return new OrderPageResult(records, total);
    }

    public List<Order> findAll() {
        return orderMapper.selectList(Wrappers.<OrderDO>lambdaQuery()
                        .orderByDesc(OrderDO::getCreatedAt, OrderDO::getId))
                .stream()
                .map(this::toDomainWithSnapshots)
                .toList();
    }

    private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OrderDO> buildPageQuery(
            OrderPageQuery query) {
        return Wrappers.<OrderDO>lambdaQuery()
                .eq(query.tenantId() != null, OrderDO::getTenantId, query.tenantId())
                .eq(query.userId() != null, OrderDO::getUserId, query.userId())
                .like(query.orderNo() != null && !query.orderNo().isBlank(), OrderDO::getOrderNo, query.orderNo())
                .eq(query.orderStatus() != null && !query.orderStatus().isBlank(),
                        OrderDO::getOrderStatus, query.orderStatus())
                .eq(query.payStatus() != null && !query.payStatus().isBlank(), OrderDO::getPayStatus, query.payStatus())
                .eq(query.inventoryStatus() != null && !query.inventoryStatus().isBlank(),
                        OrderDO::getInventoryStatus, query.inventoryStatus())
                .ge(query.createdAtFrom() != null, OrderDO::getCreatedAt, query.createdAtFrom())
                .le(query.createdAtTo() != null, OrderDO::getCreatedAt, query.createdAtTo());
    }

    private OrderDO toDataObject(Order order) {
        return new OrderDO(toDatabaseOrderId(order.getId()), order.getTenantId(), order.getOrderNo(),
                toDatabaseUserId(order.getUserId()),
                order.getOrderStatus(), order.getPayStatus(), order.getInventoryStatus(), order.getCurrencyCode(),
                order.getTotalAmount().value(), order.getPayableAmount().value(), order.getRemark(), order.getCancelReason(),
                order.getCloseReason(), order.getCreatedAt(), Instant.now(), order.getExpiredAt(), order.getPaidAt(),
                order.getClosedAt());
    }

    private Order toDomainWithSnapshots(OrderDO dataObject) {
        // 详情查询需要把主表和两张快照表重新拼成完整领域对象，保证应用层看到的是统一视图。
        OrderPaymentSnapshotDO paymentSnapshot = orderPaymentSnapshotMapper.selectOne(
                Wrappers.<OrderPaymentSnapshotDO>lambdaQuery()
                        .eq(OrderPaymentSnapshotDO::getTenantId, dataObject.getTenantId())
                        .eq(OrderPaymentSnapshotDO::getOrderId, dataObject.getId()));
        OrderInventorySnapshotDO inventorySnapshot = orderInventorySnapshotMapper.selectOne(
                Wrappers.<OrderInventorySnapshotDO>lambdaQuery()
                        .eq(OrderInventorySnapshotDO::getTenantId, dataObject.getTenantId())
                        .eq(OrderInventorySnapshotDO::getOrderId, dataObject.getId()));
        return toDomain(dataObject, paymentSnapshot, inventorySnapshot);
    }

    private Order toDomain(OrderDO dataObject, OrderPaymentSnapshotDO paymentSnapshot,
                           OrderInventorySnapshotDO inventorySnapshot) {
        // rehydrate 时优先用快照表补回 paymentNo/reservationNo 等派生字段，
        // 因为这些字段在 strict 持久化模型里并不全部固化在订单主表。
        return Order.rehydrate(toDomainOrderId(dataObject.getId()), dataObject.getTenantId(), dataObject.getOrderNo(),
                toDomainUserId(dataObject.getUserId()), toDomainOrderStatus(dataObject.getOrderStatus()), dataObject.getPayStatus(),
                dataObject.getInventoryStatus(),
                paymentSnapshot == null ? null : paymentSnapshot.getPaymentNo(),
                inventorySnapshot == null ? null : inventorySnapshot.getReservationNo(),
                toMoney(dataObject.getTotalAmount(), dataObject.getCurrencyCode()),
                toMoney(dataObject.getPayableAmount(), dataObject.getCurrencyCode()),
                dataObject.getRemark(), dataObject.getCancelReason(),
                dataObject.getCloseReason(), dataObject.getCreatedAt(), dataObject.getExpiredAt(),
                dataObject.getPaidAt(), dataObject.getClosedAt(),
                paymentSnapshot == null ? null : paymentSnapshot.getChannelCode(),
                paymentSnapshot == null ? null : toMoney(paymentSnapshot.getPaidAmount(), dataObject.getCurrencyCode()),
                paymentSnapshot == null ? null : paymentSnapshot.getChannelStatus(),
                paymentSnapshot == null ? null : paymentSnapshot.getFailureReason(),
                null,
                inventorySnapshot == null ? null : inventorySnapshot.getWarehouseId(),
                inventorySnapshot == null ? null : inventorySnapshot.getFailureReason(),
                null, null, null);
    }

    private Money toMoney(java.math.BigDecimal value, String currencyCode) {
        if (value == null || currencyCode == null || currencyCode.isBlank()) {
            return null;
        }
        return Money.of(value, CurrencyCode.fromValue(currencyCode));
    }

    private Long toDatabaseOrderId(OrderId orderId) {
        return orderId == null ? null : Long.valueOf(orderId.value());
    }

    private OrderId toDomainOrderId(Long orderId) {
        return orderId == null ? null : OrderId.of(String.valueOf(orderId));
    }

    private Long toDatabaseUserId(UserId userId) {
        return userId == null ? null : Long.valueOf(userId.value());
    }

    private UserId toDomainUserId(Long userId) {
        return userId == null ? null : UserId.of(String.valueOf(userId));
    }

    private OrderStatus toDomainOrderStatus(String orderStatus) {
        return orderStatus == null ? null : OrderStatus.fromValue(orderStatus);
    }

    private OrderItem toDomain(OrderItemDO dataObject) {
        return new OrderItem(dataObject.getTenantId(), dataObject.getOrderId(), dataObject.getSkuId(),
                dataObject.getSkuName(), dataObject.getQuantity(), dataObject.getSalePrice(), dataObject.getLineAmount());
    }

    private OrderPaymentSnapshotDO toDataObject(OrderPaymentSnapshot snapshot) {
        return new OrderPaymentSnapshotDO(snapshot.id(), snapshot.tenantId(), snapshot.orderId(),
                snapshot.paymentNo(), snapshot.channelCode(), snapshot.payStatus(), snapshot.paidAmount(),
                snapshot.paidTime(), snapshot.failureReason(), snapshot.channelStatus(), snapshot.updatedAt());
    }

    private OrderPaymentSnapshot toDomain(OrderPaymentSnapshotDO dataObject) {
        return new OrderPaymentSnapshot(dataObject.getId(), dataObject.getTenantId(), dataObject.getOrderId(),
                dataObject.getPaymentNo(), dataObject.getChannelCode(), dataObject.getPayStatus(),
                dataObject.getPaidAmount(), dataObject.getPaidTime(), dataObject.getFailureReason(),
                dataObject.getChannelStatus(), dataObject.getUpdatedAt());
    }

    private OrderInventorySnapshotDO toDataObject(OrderInventorySnapshot snapshot) {
        return new OrderInventorySnapshotDO(snapshot.id(), snapshot.tenantId(), snapshot.orderId(),
                snapshot.reservationNo(), snapshot.inventoryStatus(), snapshot.warehouseId(), snapshot.failureReason(),
                snapshot.updatedAt());
    }

    private OrderInventorySnapshot toDomain(OrderInventorySnapshotDO dataObject) {
        return new OrderInventorySnapshot(dataObject.getId(), dataObject.getTenantId(), dataObject.getOrderId(),
                dataObject.getReservationNo(), dataObject.getInventoryStatus(), dataObject.getWarehouseId(),
                dataObject.getFailureReason(), dataObject.getUpdatedAt());
    }

    private OrderAuditLogDO toDataObject(OrderAuditLog auditLog) {
        return new OrderAuditLogDO(auditLog.id(), auditLog.tenantId(), auditLog.orderNo(), auditLog.actionType(),
                auditLog.beforeStatus(), auditLog.afterStatus(), auditLog.operatorType(), auditLog.operatorId(),
                auditLog.occurredAt());
    }

    private OrderAuditLog toDomain(OrderAuditLogDO dataObject) {
        return new OrderAuditLog(dataObject.getId(), dataObject.getTenantId(), dataObject.getOrderNo(),
                dataObject.getActionType(), dataObject.getBeforeStatus(), dataObject.getAfterStatus(),
                dataObject.getOperatorType(), dataObject.getOperatorId(), dataObject.getOccurredAt());
    }
}
