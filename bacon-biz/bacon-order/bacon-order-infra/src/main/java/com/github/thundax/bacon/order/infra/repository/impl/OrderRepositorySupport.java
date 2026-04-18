package com.github.thundax.bacon.order.infra.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.order.domain.model.entity.Order;
import com.github.thundax.bacon.order.domain.model.entity.OrderItem;
import com.github.thundax.bacon.order.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.order.domain.model.enums.OrderStatus;
import com.github.thundax.bacon.order.domain.model.enums.PayStatus;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderId;
import com.github.thundax.bacon.order.infra.persistence.assembler.OrderInventorySnapshotPersistenceAssembler;
import com.github.thundax.bacon.order.infra.persistence.assembler.OrderItemPersistenceAssembler;
import com.github.thundax.bacon.order.infra.persistence.assembler.OrderPaymentSnapshotPersistenceAssembler;
import com.github.thundax.bacon.order.infra.persistence.assembler.OrderPersistenceAssembler;
import com.github.thundax.bacon.order.infra.persistence.dataobject.OrderDO;
import com.github.thundax.bacon.order.infra.persistence.dataobject.OrderInventorySnapshotDO;
import com.github.thundax.bacon.order.infra.persistence.dataobject.OrderItemDO;
import com.github.thundax.bacon.order.infra.persistence.dataobject.OrderPaymentSnapshotDO;
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
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderPaymentSnapshotMapper orderPaymentSnapshotMapper;
    private final OrderInventorySnapshotMapper orderInventorySnapshotMapper;
    private final IdGenerator idGenerator;
    private final OrderPersistenceAssembler orderPersistenceAssembler;
    private final OrderItemPersistenceAssembler orderItemPersistenceAssembler;
    private final OrderPaymentSnapshotPersistenceAssembler orderPaymentSnapshotPersistenceAssembler;
    private final OrderInventorySnapshotPersistenceAssembler orderInventorySnapshotPersistenceAssembler;

    public OrderRepositorySupport(
            OrderMapper orderMapper,
            OrderItemMapper orderItemMapper,
            OrderPaymentSnapshotMapper orderPaymentSnapshotMapper,
            OrderInventorySnapshotMapper orderInventorySnapshotMapper,
            IdGenerator idGenerator,
            OrderPersistenceAssembler orderPersistenceAssembler,
            OrderItemPersistenceAssembler orderItemPersistenceAssembler,
            OrderPaymentSnapshotPersistenceAssembler orderPaymentSnapshotPersistenceAssembler,
            OrderInventorySnapshotPersistenceAssembler orderInventorySnapshotPersistenceAssembler) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.orderPaymentSnapshotMapper = orderPaymentSnapshotMapper;
        this.orderInventorySnapshotMapper = orderInventorySnapshotMapper;
        this.idGenerator = idGenerator;
        this.orderPersistenceAssembler = orderPersistenceAssembler;
        this.orderItemPersistenceAssembler = orderItemPersistenceAssembler;
        this.orderPaymentSnapshotPersistenceAssembler = orderPaymentSnapshotPersistenceAssembler;
        this.orderInventorySnapshotPersistenceAssembler = orderInventorySnapshotPersistenceAssembler;
        log.info("Using MyBatis-Plus order repository");
    }

    public Order insert(Order order) {
        OrderDO dataObject = orderPersistenceAssembler.toDataObject(order);
        dataObject.setUpdatedAt(Instant.now());
        dataObject.setId(idGenerator.nextId(ORDER_ID_BIZ_TAG));
        orderMapper.insert(dataObject);
        return withId(order, OrderId.of(dataObject.getId()));
    }

    public Order update(Order order) {
        OrderDO dataObject = orderPersistenceAssembler.toDataObject(order);
        dataObject.setUpdatedAt(Instant.now());
        // 订单主表只承载主单核心字段；支付和库存侧派生信息不直接塞回主表，而是走快照表分开维护。
        orderMapper.updateById(dataObject);
        return order;
    }

    public Optional<Order> findById(OrderId id) {
        return Optional.ofNullable(orderMapper.selectById(toOrderIdValue(id))).map(this::toDomainWithSnapshots);
    }

    public Optional<Order> findByOrderNo(OrderNo orderNo) {
        BaconContextHolder.requireTenantId();
        return Optional.ofNullable(
                        orderMapper.selectOne(
                                Wrappers.<OrderDO>lambdaQuery().eq(OrderDO::getOrderNo, toOrderNoValue(orderNo))))
                .map(this::toDomainWithSnapshots);
    }

    public void updateItems(OrderId orderId, List<OrderItem> items) {
        BaconContextHolder.requireTenantId();
        // 订单项采用“先删后插”的整包替换策略，保持应用层传入的 items 列表就是该订单的权威快照。
        orderItemMapper.delete(Wrappers.<OrderItemDO>lambdaQuery().eq(OrderItemDO::getOrderId, toOrderIdValue(orderId)));
        if (items == null || items.isEmpty()) {
            return;
        }
        for (OrderItem item : items) {
            orderItemMapper.insert(orderItemPersistenceAssembler.toDataObject(item));
        }
    }

    public List<OrderItem> listItemsByOrderId(OrderId orderId) {
        BaconContextHolder.requireTenantId();
        return orderItemMapper
                .selectList(Wrappers.<OrderItemDO>lambdaQuery()
                        .eq(OrderItemDO::getOrderId, toOrderIdValue(orderId))
                        .orderByAsc(OrderItemDO::getId))
                .stream()
                .map(orderItemPersistenceAssembler::toDomain)
                .toList();
    }

    public List<Order> list() {
        return orderMapper
                .selectList(Wrappers.<OrderDO>lambdaQuery().orderByDesc(OrderDO::getCreatedAt, OrderDO::getId))
                .stream()
                .map(this::toDomainWithSnapshots)
                .toList();
    }

    public long count(
            UserId userId,
            OrderNo orderNo,
            OrderStatus orderStatus,
            PayStatus payStatus,
            InventoryStatus inventoryStatus,
            Instant createdAtFrom,
            Instant createdAtTo) {
        BaconContextHolder.requireTenantId();
        return Optional.ofNullable(orderMapper.selectCount(buildPageQuery(
                        userId, orderNo, orderStatus, payStatus, inventoryStatus, createdAtFrom, createdAtTo)))
                .orElse(0L);
    }

    public List<Order> page(
            UserId userId,
            OrderNo orderNo,
            OrderStatus orderStatus,
            PayStatus payStatus,
            InventoryStatus inventoryStatus,
            Instant createdAtFrom,
            Instant createdAtTo,
            int pageNo,
            int pageSize) {
        BaconContextHolder.requireTenantId();
        int normalizedPageNo = Math.max(pageNo, 1);
        int normalizedPageSize = Math.max(pageSize, 1);
        int offset = Math.max(0, (normalizedPageNo - 1) * normalizedPageSize);
        List<OrderDO> page = orderMapper.selectList(
                buildPageQuery(userId, orderNo, orderStatus, payStatus, inventoryStatus, createdAtFrom, createdAtTo)
                        .orderByDesc(OrderDO::getCreatedAt, OrderDO::getId)
                        .last("limit " + offset + "," + normalizedPageSize));
        if (page.isEmpty()) {
            return List.of();
        }
        List<String> orderNos = page.stream().map(OrderDO::getOrderNo).toList();
        List<Long> orderIds = page.stream().map(OrderDO::getId).toList();
        // 分页查询先批量拉主单，再一次性批量拉支付/库存快照，避免逐单 N+1 查询。
        Map<Long, OrderPaymentSnapshotDO> paymentSnapshotMap = orderPaymentSnapshotMapper
                .selectList(
                        Wrappers.<OrderPaymentSnapshotDO>lambdaQuery().in(OrderPaymentSnapshotDO::getOrderId, orderIds))
                .stream()
                .collect(Collectors.toMap(
                        OrderPaymentSnapshotDO::getOrderId, Function.identity(), (left, right) -> left));
        Map<String, OrderInventorySnapshotDO> inventorySnapshotMap = orderInventorySnapshotMapper
                .selectList(Wrappers.<OrderInventorySnapshotDO>lambdaQuery()
                        .in(OrderInventorySnapshotDO::getOrderNo, orderNos))
                .stream()
                .collect(Collectors.toMap(
                        OrderInventorySnapshotDO::getOrderNo, Function.identity(), (left, right) -> left));
        List<Order> records = page.stream()
                .map(orderData -> orderPersistenceAssembler.toDomain(
                        orderData,
                        paymentSnapshotMap.get(orderData.getId()),
                        inventorySnapshotMap.get(orderData.getOrderNo())))
                .toList();
        return records;
    }

    private LambdaQueryWrapper<OrderDO> buildPageQuery(
            UserId userId,
            OrderNo orderNo,
            OrderStatus orderStatus,
            PayStatus payStatus,
            InventoryStatus inventoryStatus,
            Instant createdAtFrom,
            Instant createdAtTo) {
        return Wrappers.<OrderDO>lambdaQuery()
                .eq(userId != null, OrderDO::getUserId, toUserIdValue(userId))
                .like(orderNo != null && !orderNo.value().isBlank(), OrderDO::getOrderNo, toOrderNoValue(orderNo))
                .eq(orderStatus != null, OrderDO::getOrderStatus, toOrderStatusValue(orderStatus))
                .eq(payStatus != null, OrderDO::getPayStatus, toPayStatusValue(payStatus))
                .eq(inventoryStatus != null, OrderDO::getInventoryStatus, toInventoryStatusValue(inventoryStatus))
                .ge(createdAtFrom != null, OrderDO::getCreatedAt, createdAtFrom)
                .le(createdAtTo != null, OrderDO::getCreatedAt, createdAtTo);
    }

    private Long toOrderIdValue(OrderId orderId) {
        return orderId == null ? null : orderId.value();
    }

    private Order withId(Order order, OrderId orderId) {
        return Order.reconstruct(
                orderId,
                order.getOrderNo(),
                order.getUserId(),
                order.getOrderStatus(),
                order.getPayStatus(),
                order.getInventoryStatus(),
                order.getPaymentNo(),
                order.getReservationNo(),
                order.getCurrencyCode(),
                order.getTotalAmount(),
                order.getPayableAmount(),
                order.getRemark(),
                order.getCancelReason(),
                order.getCloseReason(),
                order.getCreatedAt(),
                order.getExpiredAt(),
                order.getPaidAt(),
                order.getClosedAt(),
                order.getPaymentChannelCode(),
                order.getPaidAmount(),
                order.getPaymentChannelStatus(),
                order.getPaymentFailureReason(),
                order.getPaymentFailedAt(),
                order.getWarehouseCode(),
                order.getInventoryFailureReason(),
                order.getInventoryReleaseReason(),
                order.getInventoryReleasedAt(),
                order.getInventoryDeductedAt());
    }

    private String toOrderNoValue(OrderNo orderNo) {
        return orderNo == null ? null : orderNo.value();
    }

    private Long toUserIdValue(UserId userId) {
        return userId == null ? null : Long.valueOf(userId.value());
    }

    private String toOrderStatusValue(OrderStatus orderStatus) {
        return orderStatus == null ? null : orderStatus.value();
    }

    private String toPayStatusValue(PayStatus payStatus) {
        return payStatus == null ? null : payStatus.value();
    }

    private String toInventoryStatusValue(InventoryStatus inventoryStatus) {
        return inventoryStatus == null ? null : inventoryStatus.value();
    }

    private Order toDomainWithSnapshots(OrderDO dataObject) {
        // 详情查询需要把主表和两张快照表重新拼成完整领域对象，保证应用层看到的是统一视图。
        OrderPaymentSnapshotDO paymentSnapshot =
                orderPaymentSnapshotMapper.selectOne(Wrappers.<OrderPaymentSnapshotDO>lambdaQuery()
                        .eq(OrderPaymentSnapshotDO::getTenantId, dataObject.getTenantId())
                        .eq(OrderPaymentSnapshotDO::getOrderId, dataObject.getId()));
        OrderInventorySnapshotDO inventorySnapshot =
                orderInventorySnapshotMapper.selectOne(Wrappers.<OrderInventorySnapshotDO>lambdaQuery()
                        .eq(OrderInventorySnapshotDO::getTenantId, dataObject.getTenantId())
                        .eq(OrderInventorySnapshotDO::getOrderNo, dataObject.getOrderNo()));
        return orderPersistenceAssembler.toDomain(dataObject, paymentSnapshot, inventorySnapshot);
    }
}
