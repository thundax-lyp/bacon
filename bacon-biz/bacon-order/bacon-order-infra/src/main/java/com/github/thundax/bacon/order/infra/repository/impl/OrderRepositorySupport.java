package com.github.thundax.bacon.order.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.commerce.enums.CurrencyCode;
import com.github.thundax.bacon.common.commerce.valueobject.Money;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.order.domain.model.entity.Order;
import com.github.thundax.bacon.order.domain.model.entity.OrderAuditLog;
import com.github.thundax.bacon.order.domain.model.entity.OrderInventorySnapshot;
import com.github.thundax.bacon.order.domain.model.entity.OrderItem;
import com.github.thundax.bacon.order.domain.model.entity.OrderPaymentSnapshot;
import com.github.thundax.bacon.order.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.order.domain.model.enums.OperatorType;
import com.github.thundax.bacon.order.domain.model.enums.OrderAuditActionType;
import com.github.thundax.bacon.order.domain.model.enums.OrderStatus;
import com.github.thundax.bacon.order.domain.model.enums.PayStatus;
import com.github.thundax.bacon.order.domain.model.enums.PaymentChannel;
import com.github.thundax.bacon.order.domain.model.enums.PaymentChannelStatus;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.commerce.valueobject.PaymentNo;
import com.github.thundax.bacon.order.domain.model.valueobject.ReservationNo;
import com.github.thundax.bacon.common.commerce.valueobject.WarehouseCode;
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

    private static final String ORDER_ID_BIZ_TAG = "order-id";
    private static final String AUDIT_LOG_ID_BIZ_TAG = "order_audit_log_id";
    private static final String PAYMENT_SNAPSHOT_ID_BIZ_TAG = "order_payment_snapshot_id";
    private static final String INVENTORY_SNAPSHOT_ID_BIZ_TAG = "order_inventory_snapshot_id";
    private static final String ORDER_ITEM_ID_BIZ_TAG = "order_item_id";

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderPaymentSnapshotMapper orderPaymentSnapshotMapper;
    private final OrderInventorySnapshotMapper orderInventorySnapshotMapper;
    private final OrderAuditLogMapper orderAuditLogMapper;
    private final IdGenerator idGenerator;

    public OrderRepositorySupport(OrderMapper orderMapper,
                                  OrderItemMapper orderItemMapper,
                                  OrderPaymentSnapshotMapper orderPaymentSnapshotMapper,
                                  OrderInventorySnapshotMapper orderInventorySnapshotMapper,
                                  OrderAuditLogMapper orderAuditLogMapper,
                                  IdGenerator idGenerator) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.orderPaymentSnapshotMapper = orderPaymentSnapshotMapper;
        this.orderInventorySnapshotMapper = orderInventorySnapshotMapper;
        this.orderAuditLogMapper = orderAuditLogMapper;
        this.idGenerator = idGenerator;
        log.info("Using MyBatis-Plus order repository");
    }

    public Order saveOrder(Order order) {
        OrderDO dataObject = toDataObject(order);
        dataObject.setUpdatedAt(Instant.now());
        if (dataObject.getId() == null) {
            dataObject.setId(idGenerator.nextId(ORDER_ID_BIZ_TAG));
            orderMapper.insert(dataObject);
            order.setId(OrderId.of(dataObject.getId()));
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
                .eq(OrderItemDO::getOrderId, String.valueOf(orderId)));
        if (items == null || items.isEmpty()) {
            return;
        }
        for (OrderItem item : items) {
            orderItemMapper.insert(new OrderItemDO(idGenerator.nextId(ORDER_ITEM_ID_BIZ_TAG), item.getTenantIdValue(),
                    String.valueOf(item.getOrderIdValue()), String.valueOf(item.getSkuId() == null ? null : item.getSkuId().value()),
                    item.getSkuName(), item.getImageUrl(), item.getQuantity(), item.getSalePrice().value(),
                    item.getLineAmount().value()));
        }
    }

    public List<OrderItem> findItemsByOrderId(Long tenantId, Long orderId, String currencyCode) {
        return orderItemMapper.selectList(Wrappers.<OrderItemDO>lambdaQuery()
                        .eq(OrderItemDO::getTenantId, tenantId)
                        .eq(OrderItemDO::getOrderId, String.valueOf(orderId))
                        .orderByAsc(OrderItemDO::getId))
                .stream()
                .map(dataObject -> toDomain(dataObject, currencyCode))
                .toList();
    }

    public void savePaymentSnapshot(OrderPaymentSnapshot snapshot) {
        OrderPaymentSnapshotDO existing = orderPaymentSnapshotMapper.selectOne(
                Wrappers.<OrderPaymentSnapshotDO>lambdaQuery()
                        .eq(OrderPaymentSnapshotDO::getTenantId, snapshot.tenantIdValue())
                        .eq(OrderPaymentSnapshotDO::getOrderId, snapshot.orderIdValue()));
        OrderPaymentSnapshotDO dataObject = toDataObject(snapshot);
        dataObject.setUpdatedAt(snapshot.updatedAt() == null ? Instant.now() : snapshot.updatedAt());
        // 支付快照按 orderId 唯一覆盖，目标是保留“当前支付视图”，而不是积累每次变化历史。
        if (existing == null) {
            dataObject.setId(idGenerator.nextId(PAYMENT_SNAPSHOT_ID_BIZ_TAG));
            orderPaymentSnapshotMapper.insert(dataObject);
            return;
        }
        dataObject.setId(existing.getId());
        orderPaymentSnapshotMapper.updateById(dataObject);
    }

    public Optional<OrderPaymentSnapshot> findPaymentSnapshotByOrderId(Long tenantId, Long orderId, String currencyCode) {
        return Optional.ofNullable(orderPaymentSnapshotMapper.selectOne(
                Wrappers.<OrderPaymentSnapshotDO>lambdaQuery()
                        .eq(OrderPaymentSnapshotDO::getTenantId, tenantId)
                        .eq(OrderPaymentSnapshotDO::getOrderId, orderId)))
                .map(dataObject -> toDomain(dataObject, currencyCode));
    }

    public void saveInventorySnapshot(OrderInventorySnapshot snapshot) {
        OrderInventorySnapshotDO existing = orderInventorySnapshotMapper.selectOne(
                Wrappers.<OrderInventorySnapshotDO>lambdaQuery()
                        .eq(OrderInventorySnapshotDO::getTenantId, snapshot.tenantIdValue())
                        .eq(OrderInventorySnapshotDO::getOrderNo, snapshot.orderNoValue()));
        OrderInventorySnapshotDO dataObject = toDataObject(snapshot);
        dataObject.setUpdatedAt(snapshot.updatedAt() == null ? Instant.now() : snapshot.updatedAt());
        // 库存快照和支付快照一样采用唯一覆盖模型，分页/详情查询只需要当前库存派生状态。
        if (existing == null) {
            dataObject.setId(idGenerator.nextId(INVENTORY_SNAPSHOT_ID_BIZ_TAG));
            orderInventorySnapshotMapper.insert(dataObject);
            return;
        }
        dataObject.setId(existing.getId());
        orderInventorySnapshotMapper.updateById(dataObject);
    }

    public Optional<OrderInventorySnapshot> findInventorySnapshotByOrderNo(Long tenantId, String orderNo) {
        return Optional.ofNullable(orderInventorySnapshotMapper.selectOne(
                Wrappers.<OrderInventorySnapshotDO>lambdaQuery()
                        .eq(OrderInventorySnapshotDO::getTenantId, tenantId)
                        .eq(OrderInventorySnapshotDO::getOrderNo, orderNo)))
                .map(this::toDomain);
    }

    public void saveAuditLog(OrderAuditLog auditLog) {
        OrderAuditLogDO dataObject = toDataObject(auditLog);
        if (dataObject.getId() == null) {
            dataObject.setId(idGenerator.nextId(AUDIT_LOG_ID_BIZ_TAG));
        }
        orderAuditLogMapper.insert(dataObject);
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

    public long countOrders(Long tenantId, Long userId, String orderNo, String orderStatus, String payStatus,
                            String inventoryStatus, Instant createdAtFrom, Instant createdAtTo) {
        return Optional.ofNullable(orderMapper.selectCount(buildPageQuery(tenantId, userId, orderNo, orderStatus,
                payStatus, inventoryStatus, createdAtFrom, createdAtTo))).orElse(0L);
    }

    public List<Order> pageOrders(Long tenantId, Long userId, String orderNo, String orderStatus, String payStatus,
                                  String inventoryStatus, Instant createdAtFrom, Instant createdAtTo,
                                  int offset, int limit) {
        List<OrderDO> pageOrders = orderMapper.selectList(buildPageQuery(tenantId, userId, orderNo, orderStatus,
                        payStatus, inventoryStatus, createdAtFrom, createdAtTo)
                .orderByDesc(OrderDO::getCreatedAt, OrderDO::getId)
                .last("limit " + offset + "," + limit));
        if (pageOrders.isEmpty()) {
            return List.of();
        }
        List<String> orderNos = pageOrders.stream()
                .map(OrderDO::getOrderNo)
                .toList();
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
        Map<String, OrderInventorySnapshotDO> inventorySnapshotMap = orderInventorySnapshotMapper.selectList(
                        Wrappers.<OrderInventorySnapshotDO>lambdaQuery()
                                .in(OrderInventorySnapshotDO::getOrderNo, orderNos))
                .stream()
                .collect(Collectors.toMap(OrderInventorySnapshotDO::getOrderNo, Function.identity(),
                        (left, right) -> left));
        List<Order> records = pageOrders.stream()
                .map(orderData -> toDomain(orderData, paymentSnapshotMap.get(orderData.getId()),
                        inventorySnapshotMap.get(orderData.getOrderNo())))
                .toList();
        return records;
    }

    public List<Order> findAll() {
        return orderMapper.selectList(Wrappers.<OrderDO>lambdaQuery()
                        .orderByDesc(OrderDO::getCreatedAt, OrderDO::getId))
                .stream()
                .map(this::toDomainWithSnapshots)
                .toList();
    }

    private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OrderDO> buildPageQuery(
            Long tenantId, Long userId, String orderNo, String orderStatus, String payStatus,
            String inventoryStatus, Instant createdAtFrom, Instant createdAtTo) {
        return Wrappers.<OrderDO>lambdaQuery()
                .eq(tenantId != null, OrderDO::getTenantId, tenantId)
                .eq(userId != null, OrderDO::getUserId, userId)
                .like(orderNo != null && !orderNo.isBlank(), OrderDO::getOrderNo, orderNo)
                .eq(orderStatus != null && !orderStatus.isBlank(), OrderDO::getOrderStatus, orderStatus)
                .eq(payStatus != null && !payStatus.isBlank(), OrderDO::getPayStatus, payStatus)
                .eq(inventoryStatus != null && !inventoryStatus.isBlank(), OrderDO::getInventoryStatus, inventoryStatus)
                .ge(createdAtFrom != null, OrderDO::getCreatedAt, createdAtFrom)
                .le(createdAtTo != null, OrderDO::getCreatedAt, createdAtTo);
    }

    private OrderDO toDataObject(Order order) {
        return new OrderDO(order.getId() == null ? null : order.getId().value(),
                order.getTenantId() == null ? null : order.getTenantId().value(),
                toDatabaseOrderNo(order.getOrderNo()),
                order.getUserId() == null ? null : order.getUserId().value(),
                order.getOrderStatusValue(), order.getPayStatusValue(), order.getInventoryStatusValue(), order.getCurrencyCodeValue(),
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
                        .eq(OrderInventorySnapshotDO::getOrderNo, dataObject.getOrderNo()));
        return toDomain(dataObject, paymentSnapshot, inventorySnapshot);
    }

    private Order toDomain(OrderDO dataObject, OrderPaymentSnapshotDO paymentSnapshot,
                           OrderInventorySnapshotDO inventorySnapshot) {
        // rehydrate 时优先用快照表补回 paymentNo/reservationNo 等派生字段，
        // 因为这些字段在 strict 持久化模型里并不全部固化在订单主表。
        return Order.rehydrate(toDomainOrderId(dataObject.getId()), toDomainOrderTenantId(dataObject.getTenantId()),
                toDomainOrderNo(dataObject.getOrderNo()), toDomainOrderUserId(dataObject.getUserId()),
                toDomainOrderStatus(dataObject.getOrderStatus()),
                dataObject.getPayStatus() == null ? null : PayStatus.from(dataObject.getPayStatus()),
                dataObject.getInventoryStatus() == null ? null : InventoryStatus.from(dataObject.getInventoryStatus()),
                toDomainPaymentNo(paymentSnapshot == null ? null : paymentSnapshot.getPaymentNo()),
                inventorySnapshot == null || inventorySnapshot.getReservationNo() == null
                        ? null
                        : ReservationNo.of(inventorySnapshot.getReservationNo()),
                dataObject.getCurrencyCode() == null ? null : CurrencyCode.fromValue(dataObject.getCurrencyCode()),
                toMoney(dataObject.getTotalAmount(), dataObject.getCurrencyCode()),
                toMoney(dataObject.getPayableAmount(), dataObject.getCurrencyCode()),
                dataObject.getRemark(), dataObject.getCancelReason(),
                dataObject.getCloseReason(), dataObject.getCreatedAt(), dataObject.getExpiredAt(),
                dataObject.getPaidAt(), dataObject.getClosedAt(),
                paymentSnapshot == null || paymentSnapshot.getChannelCode() == null
                        ? null
                        : PaymentChannel.from(paymentSnapshot.getChannelCode()),
                paymentSnapshot == null ? null : toMoney(paymentSnapshot.getPaidAmount(), dataObject.getCurrencyCode()),
                paymentSnapshot == null ? null : paymentSnapshot.getChannelStatus(),
                paymentSnapshot == null ? null : paymentSnapshot.getFailureReason(),
                null,
                inventorySnapshot == null || inventorySnapshot.getWarehouseCode() == null
                        ? null
                        : WarehouseCode.of(inventorySnapshot.getWarehouseCode()),
                inventorySnapshot == null ? null : inventorySnapshot.getFailureReason(),
                null, null, null);
    }

    private Money toMoney(java.math.BigDecimal value, String currencyCode) {
        if (value == null || currencyCode == null || currencyCode.isBlank()) {
            return null;
        }
        return Money.of(value, CurrencyCode.fromValue(currencyCode));
    }

    private OrderId toDomainOrderId(Long orderId) {
        return orderId == null ? null : OrderId.of(orderId);
    }

    private String toDatabaseOrderNo(OrderNo orderNo) {
        return orderNo == null ? null : orderNo.value();
    }

    private OrderNo toDomainOrderNo(String orderNo) {
        return orderNo == null ? null : OrderNo.of(orderNo);
    }

    private PaymentNo toDomainPaymentNo(String paymentNo) {
        return paymentNo == null ? null : PaymentNo.of(paymentNo);
    }

    private TenantId toDomainOrderTenantId(Long tenantId) {
        return tenantId == null ? null : TenantId.of(tenantId);
    }

    private UserId toDomainOrderUserId(Long userId) {
        return userId == null ? null : UserId.of(userId);
    }

    private OrderStatus toDomainOrderStatus(String orderStatus) {
        return orderStatus == null ? null : OrderStatus.from(orderStatus);
    }

    private OrderItem toDomain(OrderItemDO dataObject, String currencyCode) {
        CurrencyCode resolvedCurrencyCode = CurrencyCode.fromValue(currencyCode);
        return new OrderItem(dataObject.getTenantId(),
                dataObject.getOrderId() == null ? null : Long.valueOf(dataObject.getOrderId()),
                dataObject.getSkuId() == null ? null : Long.valueOf(dataObject.getSkuId()),
                dataObject.getSkuName(), dataObject.getImageUrl(),
                dataObject.getQuantity(), resolvedCurrencyCode,
                dataObject.getSalePrice() == null ? null : dataObject.getSalePrice().toPlainString(),
                dataObject.getLineAmount() == null ? null : dataObject.getLineAmount().toPlainString());
    }

    private OrderPaymentSnapshotDO toDataObject(OrderPaymentSnapshot snapshot) {
        return new OrderPaymentSnapshotDO(snapshot.id(), snapshot.tenantIdValue(), snapshot.orderIdValue(),
                snapshot.paymentNoValue(), snapshot.channelCodeValue(), snapshot.payStatusValue(), snapshot.paidAmountValue(),
                snapshot.paidTime(), snapshot.failureReason(), snapshot.channelStatusValue(), snapshot.updatedAt());
    }

    private OrderPaymentSnapshot toDomain(OrderPaymentSnapshotDO dataObject, String currencyCode) {
        return new OrderPaymentSnapshot(dataObject.getId(), toDomainOrderTenantId(dataObject.getTenantId()),
                toDomainOrderId(dataObject.getOrderId()), toDomainPaymentNo(dataObject.getPaymentNo()),
                dataObject.getChannelCode() == null ? null : PaymentChannel.from(dataObject.getChannelCode()),
                dataObject.getPayStatus() == null ? null : PayStatus.from(dataObject.getPayStatus()),
                toMoney(dataObject.getPaidAmount(), currencyCode), dataObject.getPaidTime(), dataObject.getFailureReason(),
                dataObject.getChannelStatus() == null ? null : PaymentChannelStatus.from(dataObject.getChannelStatus()),
                dataObject.getUpdatedAt());
    }

    private OrderInventorySnapshotDO toDataObject(OrderInventorySnapshot snapshot) {
        return new OrderInventorySnapshotDO(null, snapshot.tenantIdValue(), snapshot.orderNoValue(),
                snapshot.reservationNoValue(), snapshot.inventoryStatusValue(),
                snapshot.warehouseCode() == null ? null : snapshot.warehouseCode().value(),
                snapshot.failureReason(),
                snapshot.updatedAt());
    }

    private OrderInventorySnapshot toDomain(OrderInventorySnapshotDO dataObject) {
        return new OrderInventorySnapshot(toDomainOrderTenantId(dataObject.getTenantId()), toDomainOrderNo(dataObject.getOrderNo()),
                dataObject.getReservationNo() == null ? null : ReservationNo.of(dataObject.getReservationNo()),
                dataObject.getInventoryStatus() == null ? null : InventoryStatus.from(dataObject.getInventoryStatus()),
                dataObject.getWarehouseCode() == null ? null : WarehouseCode.of(dataObject.getWarehouseCode()),
                dataObject.getFailureReason(), dataObject.getUpdatedAt());
    }

    private OrderAuditLogDO toDataObject(OrderAuditLog auditLog) {
        return new OrderAuditLogDO(auditLog.id(), auditLog.tenantId() == null ? null : auditLog.tenantId().value(),
                toDatabaseOrderNo(auditLog.orderNo()),
                auditLog.actionType() == null ? null : auditLog.actionType().value(),
                auditLog.beforeStatus() == null ? null : auditLog.beforeStatus().value(),
                auditLog.afterStatus() == null ? null : auditLog.afterStatus().value(),
                auditLog.operatorType() == null ? null : auditLog.operatorType().value(),
                auditLog.operatorId(), auditLog.occurredAt());
    }

    private OrderAuditLog toDomain(OrderAuditLogDO dataObject) {
        return new OrderAuditLog(dataObject.getId(),
                dataObject.getTenantId() == null ? null : TenantId.of(dataObject.getTenantId()),
                toDomainOrderNo(dataObject.getOrderNo()),
                dataObject.getActionType() == null ? null : OrderAuditActionType.from(dataObject.getActionType()),
                toDomainOrderStatus(dataObject.getBeforeStatus()),
                toDomainOrderStatus(dataObject.getAfterStatus()),
                dataObject.getOperatorType() == null ? null : OperatorType.from(dataObject.getOperatorType()),
                dataObject.getOperatorId(), dataObject.getOccurredAt());
    }

}
