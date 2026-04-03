package com.github.thundax.bacon.order.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.core.enums.CurrencyCode;
import com.github.thundax.bacon.common.core.valueobject.Money;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.domain.OrderId;
import com.github.thundax.bacon.common.id.domain.SkuId;
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
import com.github.thundax.bacon.order.domain.model.valueobject.OrderNo;
import com.github.thundax.bacon.order.domain.model.valueobject.PaymentNo;
import com.github.thundax.bacon.order.domain.model.valueobject.ReservationNo;
import com.github.thundax.bacon.order.domain.model.valueobject.WarehouseNo;
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

    private static final String AUDIT_LOG_ID_BIZ_TAG = "order_audit_log_id";

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
            orderItemMapper.insert(new OrderItemDO(null, item.getTenantIdValue(), item.getOrderIdValue(), item.getSkuIdValue(),
                    item.getSkuName(), item.getImageUrl(), item.getQuantity(), item.getSalePrice().value(),
                    item.getLineAmount().value()));
        }
    }

    public List<OrderItem> findItemsByOrderId(Long tenantId, Long orderId, String currencyCode) {
        return orderItemMapper.selectList(Wrappers.<OrderItemDO>lambdaQuery()
                        .eq(OrderItemDO::getTenantId, tenantId)
                        .eq(OrderItemDO::getOrderId, orderId)
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
                        .eq(OrderAuditLogDO::getTenantId, toDatabaseAuditTenantId(tenantId))
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
        return new OrderDO(toDatabaseOrderId(order.getId()), toDatabaseTenantId(order.getTenantId()), toDatabaseOrderNo(order.getOrderNo()),
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
                        .eq(OrderInventorySnapshotDO::getOrderNo, dataObject.getOrderNo()));
        return toDomain(dataObject, paymentSnapshot, inventorySnapshot);
    }

    private Order toDomain(OrderDO dataObject, OrderPaymentSnapshotDO paymentSnapshot,
                           OrderInventorySnapshotDO inventorySnapshot) {
        // rehydrate 时优先用快照表补回 paymentNo/reservationNo 等派生字段，
        // 因为这些字段在 strict 持久化模型里并不全部固化在订单主表。
        return Order.rehydrate(toDomainOrderId(dataObject.getId()), toDomainTenantId(dataObject.getTenantId()), toDomainOrderNo(dataObject.getOrderNo()),
                toDomainUserId(dataObject.getUserId()), toDomainOrderStatus(dataObject.getOrderStatus()),
                toDomainPayStatus(dataObject.getPayStatus()), toDomainInventoryStatus(dataObject.getInventoryStatus()),
                toDomainPaymentNo(paymentSnapshot == null ? null : paymentSnapshot.getPaymentNo()),
                toDomainReservationNo(inventorySnapshot == null ? null : inventorySnapshot.getReservationNo()),
                toMoney(dataObject.getTotalAmount(), dataObject.getCurrencyCode()),
                toMoney(dataObject.getPayableAmount(), dataObject.getCurrencyCode()),
                dataObject.getRemark(), dataObject.getCancelReason(),
                dataObject.getCloseReason(), dataObject.getCreatedAt(), dataObject.getExpiredAt(),
                dataObject.getPaidAt(), dataObject.getClosedAt(),
                toDomainPaymentChannel(paymentSnapshot == null ? null : paymentSnapshot.getChannelCode()),
                paymentSnapshot == null ? null : toMoney(paymentSnapshot.getPaidAmount(), dataObject.getCurrencyCode()),
                paymentSnapshot == null ? null : paymentSnapshot.getChannelStatus(),
                paymentSnapshot == null ? null : paymentSnapshot.getFailureReason(),
                null,
                inventorySnapshot == null ? null : toDomainWarehouseNo(inventorySnapshot.getWarehouseNo()),
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

    private Long toDatabaseTenantId(TenantId tenantId) {
        return tenantId == null ? null : Long.valueOf(tenantId.value());
    }

    private TenantId toDomainTenantId(Long tenantId) {
        return tenantId == null ? null : TenantId.of(String.valueOf(tenantId));
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

    private ReservationNo toDomainReservationNo(String reservationNo) {
        return reservationNo == null ? null : ReservationNo.of(reservationNo);
    }

    private PaymentChannel toDomainPaymentChannel(String paymentChannelCode) {
        return paymentChannelCode == null ? null : PaymentChannel.fromValue(paymentChannelCode);
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

    private PayStatus toDomainPayStatus(String payStatus) {
        return payStatus == null ? null : PayStatus.fromValue(payStatus);
    }

    private InventoryStatus toDomainInventoryStatus(String inventoryStatus) {
        return inventoryStatus == null ? null : InventoryStatus.fromValue(inventoryStatus);
    }

    private OrderItem toDomain(OrderItemDO dataObject, String currencyCode) {
        CurrencyCode resolvedCurrencyCode = CurrencyCode.fromValue(currencyCode);
        return new OrderItem(toDomainTenantId(dataObject.getTenantId()), toDomainOrderId(dataObject.getOrderId()),
                toDomainSkuId(dataObject.getSkuId()), dataObject.getSkuName(), dataObject.getImageUrl(),
                dataObject.getQuantity(), Money.of(dataObject.getSalePrice(), resolvedCurrencyCode),
                Money.of(dataObject.getLineAmount(), resolvedCurrencyCode));
    }

    private SkuId toDomainSkuId(Long skuId) {
        return skuId == null ? null : SkuId.of(skuId);
    }

    private OrderPaymentSnapshotDO toDataObject(OrderPaymentSnapshot snapshot) {
        return new OrderPaymentSnapshotDO(snapshot.id(), snapshot.tenantIdValue(), snapshot.orderIdValue(),
                snapshot.paymentNoValue(), snapshot.channelCodeValue(), snapshot.payStatusValue(), snapshot.paidAmountValue(),
                snapshot.paidTime(), snapshot.failureReason(), snapshot.channelStatusValue(), snapshot.updatedAt());
    }

    private OrderPaymentSnapshot toDomain(OrderPaymentSnapshotDO dataObject, String currencyCode) {
        return new OrderPaymentSnapshot(dataObject.getId(), toDomainTenantId(dataObject.getTenantId()),
                toDomainOrderId(dataObject.getOrderId()), toDomainPaymentNo(dataObject.getPaymentNo()),
                toDomainPaymentChannel(dataObject.getChannelCode()), toDomainPayStatus(dataObject.getPayStatus()),
                toMoney(dataObject.getPaidAmount(), currencyCode), dataObject.getPaidTime(), dataObject.getFailureReason(),
                toDomainPaymentChannelStatus(dataObject.getChannelStatus()), dataObject.getUpdatedAt());
    }

    private PaymentChannelStatus toDomainPaymentChannelStatus(String channelStatus) {
        return channelStatus == null ? null : PaymentChannelStatus.fromValue(channelStatus);
    }

    private OrderInventorySnapshotDO toDataObject(OrderInventorySnapshot snapshot) {
        return new OrderInventorySnapshotDO(null, snapshot.tenantIdValue(), snapshot.orderNoValue(),
                snapshot.reservationNoValue(), snapshot.inventoryStatusValue(), toDatabaseWarehouseNo(snapshot.warehouseNo()),
                snapshot.failureReason(),
                snapshot.updatedAt());
    }

    private OrderInventorySnapshot toDomain(OrderInventorySnapshotDO dataObject) {
        return new OrderInventorySnapshot(toDomainTenantId(dataObject.getTenantId()), toDomainOrderNo(dataObject.getOrderNo()),
                toDomainReservationNo(dataObject.getReservationNo()), toDomainInventoryStatus(dataObject.getInventoryStatus()),
                toDomainWarehouseNo(dataObject.getWarehouseNo()),
                dataObject.getFailureReason(), dataObject.getUpdatedAt());
    }

    private OrderAuditLogDO toDataObject(OrderAuditLog auditLog) {
        return new OrderAuditLogDO(auditLog.id(), toDatabaseAuditTenantId(auditLog.tenantId()), toDatabaseOrderNo(auditLog.orderNo()),
                toDatabaseOrderAuditActionType(auditLog.actionType()), toDatabaseOrderStatus(auditLog.beforeStatus()),
                toDatabaseOrderStatus(auditLog.afterStatus()), toDatabaseOperatorType(auditLog.operatorType()),
                auditLog.operatorId(), auditLog.occurredAt());
    }

    private OrderAuditLog toDomain(OrderAuditLogDO dataObject) {
        return new OrderAuditLog(dataObject.getId(), toDomainAuditTenantId(dataObject.getTenantId()), toDomainOrderNo(dataObject.getOrderNo()),
                toDomainOrderAuditActionType(dataObject.getActionType()), toDomainOrderStatus(dataObject.getBeforeStatus()),
                toDomainOrderStatus(dataObject.getAfterStatus()), toDomainOperatorType(dataObject.getOperatorType()),
                dataObject.getOperatorId(), dataObject.getOccurredAt());
    }

    private String toDatabaseAuditTenantId(TenantId tenantId) {
        return tenantId == null ? null : tenantId.value();
    }

    private String toDatabaseAuditTenantId(Long tenantId) {
        return tenantId == null ? null : String.valueOf(tenantId);
    }

    private TenantId toDomainAuditTenantId(String tenantId) {
        return tenantId == null ? null : TenantId.of(tenantId);
    }

    private String toDatabaseOrderAuditActionType(OrderAuditActionType actionType) {
        return actionType == null ? null : actionType.value();
    }

    private OrderAuditActionType toDomainOrderAuditActionType(String actionType) {
        return actionType == null ? null : OrderAuditActionType.fromValue(actionType);
    }

    private String toDatabaseOrderStatus(OrderStatus orderStatus) {
        return orderStatus == null ? null : orderStatus.value();
    }

    private String toDatabaseOperatorType(OperatorType operatorType) {
        return operatorType == null ? null : operatorType.value();
    }

    private OperatorType toDomainOperatorType(String operatorType) {
        return operatorType == null ? null : OperatorType.fromValue(operatorType);
    }

    private String toDatabaseWarehouseNo(WarehouseNo warehouseNo) {
        return warehouseNo == null ? null : warehouseNo.value();
    }

    private WarehouseNo toDomainWarehouseNo(String warehouseNo) {
        return warehouseNo == null ? null : WarehouseNo.of(warehouseNo);
    }
}
