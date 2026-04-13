package com.github.thundax.bacon.order.infra.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.github.thundax.bacon.common.commerce.enums.CurrencyCode;
import com.github.thundax.bacon.common.commerce.valueobject.Money;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.commerce.valueobject.PaymentNo;
import com.github.thundax.bacon.common.commerce.valueobject.WarehouseCode;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.order.domain.model.entity.Order;
import com.github.thundax.bacon.order.domain.model.entity.OrderAuditLog;
import com.github.thundax.bacon.order.domain.model.entity.OrderInventorySnapshot;
import com.github.thundax.bacon.order.domain.model.entity.OrderItem;
import com.github.thundax.bacon.order.domain.model.entity.OrderPaymentSnapshot;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderId;
import com.github.thundax.bacon.order.infra.persistence.assembler.OrderAuditLogPersistenceAssembler;
import com.github.thundax.bacon.order.infra.persistence.assembler.OrderInventorySnapshotPersistenceAssembler;
import com.github.thundax.bacon.order.infra.persistence.assembler.OrderItemPersistenceAssembler;
import com.github.thundax.bacon.order.infra.persistence.assembler.OrderPaymentSnapshotPersistenceAssembler;
import com.github.thundax.bacon.order.infra.persistence.assembler.OrderPersistenceAssembler;
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
    private final OrderPersistenceAssembler orderPersistenceAssembler;
    private final OrderItemPersistenceAssembler orderItemPersistenceAssembler;
    private final OrderPaymentSnapshotPersistenceAssembler orderPaymentSnapshotPersistenceAssembler;
    private final OrderInventorySnapshotPersistenceAssembler orderInventorySnapshotPersistenceAssembler;
    private final OrderAuditLogPersistenceAssembler orderAuditLogPersistenceAssembler;

    public OrderRepositorySupport(
            OrderMapper orderMapper,
            OrderItemMapper orderItemMapper,
            OrderPaymentSnapshotMapper orderPaymentSnapshotMapper,
            OrderInventorySnapshotMapper orderInventorySnapshotMapper,
            OrderAuditLogMapper orderAuditLogMapper,
            IdGenerator idGenerator,
            OrderPersistenceAssembler orderPersistenceAssembler,
            OrderItemPersistenceAssembler orderItemPersistenceAssembler,
            OrderPaymentSnapshotPersistenceAssembler orderPaymentSnapshotPersistenceAssembler,
            OrderInventorySnapshotPersistenceAssembler orderInventorySnapshotPersistenceAssembler,
            OrderAuditLogPersistenceAssembler orderAuditLogPersistenceAssembler) {
        this.orderMapper = orderMapper;
        this.orderItemMapper = orderItemMapper;
        this.orderPaymentSnapshotMapper = orderPaymentSnapshotMapper;
        this.orderInventorySnapshotMapper = orderInventorySnapshotMapper;
        this.orderAuditLogMapper = orderAuditLogMapper;
        this.idGenerator = idGenerator;
        this.orderPersistenceAssembler = orderPersistenceAssembler;
        this.orderItemPersistenceAssembler = orderItemPersistenceAssembler;
        this.orderPaymentSnapshotPersistenceAssembler = orderPaymentSnapshotPersistenceAssembler;
        this.orderInventorySnapshotPersistenceAssembler = orderInventorySnapshotPersistenceAssembler;
        this.orderAuditLogPersistenceAssembler = orderAuditLogPersistenceAssembler;
        log.info("Using MyBatis-Plus order repository");
    }

    public Order saveOrder(Order order) {
        OrderDO dataObject = orderPersistenceAssembler.toDataObject(order, requireTenantId());
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

    public Optional<Order> findOrderByOrderNo(String orderNo) {
        Long tenantId = requireTenantId();
        return Optional.ofNullable(orderMapper.selectOne(Wrappers.<OrderDO>lambdaQuery()
                        .eq(OrderDO::getTenantId, tenantId)
                        .eq(OrderDO::getOrderNo, orderNo)))
                .map(this::toDomainWithSnapshots);
    }

    public void saveItems(Long orderId, List<OrderItem> items) {
        Long tenantId = requireTenantId();
        // 订单项采用“先删后插”的整包替换策略，保持应用层传入的 items 列表就是该订单的权威快照。
        orderItemMapper.delete(Wrappers.<OrderItemDO>lambdaQuery()
                .eq(OrderItemDO::getTenantId, tenantId)
                .eq(OrderItemDO::getOrderId, orderId));
        if (items == null || items.isEmpty()) {
            return;
        }
        for (OrderItem item : items) {
            orderItemMapper.insert(
                    orderItemPersistenceAssembler.toDataObject(item, idGenerator.nextId(ORDER_ITEM_ID_BIZ_TAG), tenantId));
        }
    }

    public List<OrderItem> findItemsByOrderId(Long orderId, String currencyCode) {
        Long tenantId = requireTenantId();
        return orderItemMapper
                .selectList(Wrappers.<OrderItemDO>lambdaQuery()
                        .eq(OrderItemDO::getTenantId, tenantId)
                        .eq(OrderItemDO::getOrderId, orderId)
                        .orderByAsc(OrderItemDO::getId))
                .stream()
                .map(dataObject -> orderItemPersistenceAssembler.toDomain(dataObject, currencyCode))
                .toList();
    }

    public void savePaymentSnapshot(OrderPaymentSnapshot snapshot) {
        Long tenantId = requireTenantId();
        OrderPaymentSnapshotDO existing =
                orderPaymentSnapshotMapper.selectOne(Wrappers.<OrderPaymentSnapshotDO>lambdaQuery()
                        .eq(OrderPaymentSnapshotDO::getTenantId, tenantId)
                        .eq(
                                OrderPaymentSnapshotDO::getOrderId,
                                snapshot.getOrderId() == null ? null : snapshot.getOrderId().value()));
        OrderPaymentSnapshotDO dataObject = orderPaymentSnapshotPersistenceAssembler.toDataObject(snapshot, tenantId);
        dataObject.setUpdatedAt(snapshot.getUpdatedAt() == null ? Instant.now() : snapshot.getUpdatedAt());
        // 支付快照按 orderId 唯一覆盖，目标是保留“当前支付视图”，而不是积累每次变化历史。
        if (existing == null) {
            dataObject.setId(idGenerator.nextId(PAYMENT_SNAPSHOT_ID_BIZ_TAG));
            orderPaymentSnapshotMapper.insert(dataObject);
            return;
        }
        dataObject.setId(existing.getId());
        orderPaymentSnapshotMapper.updateById(dataObject);
    }

    public Optional<OrderPaymentSnapshot> findPaymentSnapshotByOrderId(Long orderId, String currencyCode) {
        Long tenantId = requireTenantId();
        return Optional.ofNullable(orderPaymentSnapshotMapper.selectOne(Wrappers.<OrderPaymentSnapshotDO>lambdaQuery()
                        .eq(OrderPaymentSnapshotDO::getTenantId, tenantId)
                        .eq(OrderPaymentSnapshotDO::getOrderId, orderId)))
                .map(dataObject -> orderPaymentSnapshotPersistenceAssembler.toDomain(dataObject, currencyCode));
    }

    public void saveInventorySnapshot(OrderInventorySnapshot snapshot) {
        Long tenantId = requireTenantId();
        OrderInventorySnapshotDO existing =
                orderInventorySnapshotMapper.selectOne(Wrappers.<OrderInventorySnapshotDO>lambdaQuery()
                        .eq(OrderInventorySnapshotDO::getTenantId, tenantId)
                        .eq(
                                OrderInventorySnapshotDO::getOrderNo,
                                snapshot.getOrderNo() == null ? null : snapshot.getOrderNo().value()));
        OrderInventorySnapshotDO dataObject =
                orderInventorySnapshotPersistenceAssembler.toDataObject(snapshot, null, tenantId);
        dataObject.setUpdatedAt(snapshot.getUpdatedAt() == null ? Instant.now() : snapshot.getUpdatedAt());
        // 库存快照和支付快照一样采用唯一覆盖模型，分页/详情查询只需要当前库存派生状态。
        if (existing == null) {
            dataObject.setId(idGenerator.nextId(INVENTORY_SNAPSHOT_ID_BIZ_TAG));
            orderInventorySnapshotMapper.insert(dataObject);
            return;
        }
        dataObject.setId(existing.getId());
        orderInventorySnapshotMapper.updateById(dataObject);
    }

    public Optional<OrderInventorySnapshot> findInventorySnapshotByOrderNo(String orderNo) {
        Long tenantId = requireTenantId();
        return Optional.ofNullable(
                orderInventorySnapshotMapper.selectOne(Wrappers.<OrderInventorySnapshotDO>lambdaQuery()
                                .eq(OrderInventorySnapshotDO::getTenantId, tenantId)
                                .eq(OrderInventorySnapshotDO::getOrderNo, orderNo)))
                .map(orderInventorySnapshotPersistenceAssembler::toDomain);
    }

    public void saveAuditLog(OrderAuditLog auditLog) {
        OrderAuditLogDO dataObject = orderAuditLogPersistenceAssembler.toDataObject(auditLog, requireTenantId());
        if (dataObject.getId() == null) {
            dataObject.setId(idGenerator.nextId(AUDIT_LOG_ID_BIZ_TAG));
        }
        orderAuditLogMapper.insert(dataObject);
    }

    public List<OrderAuditLog> findAuditLogs(String orderNo) {
        Long tenantId = requireTenantId();
        return orderAuditLogMapper
                .selectList(Wrappers.<OrderAuditLogDO>lambdaQuery()
                        .eq(OrderAuditLogDO::getTenantId, tenantId)
                        .eq(OrderAuditLogDO::getOrderNo, orderNo)
                        .orderByAsc(OrderAuditLogDO::getOccurredAt, OrderAuditLogDO::getId))
                .stream()
                .map(orderAuditLogPersistenceAssembler::toDomain)
                .toList();
    }

    public long countOrders(
            Long userId,
            String orderNo,
            String orderStatus,
            String payStatus,
            String inventoryStatus,
            Instant createdAtFrom,
            Instant createdAtTo) {
        Long tenantId = requireTenantId();
        return Optional.ofNullable(orderMapper.selectCount(buildPageQuery(
                        tenantId,
                        userId,
                        orderNo,
                        orderStatus,
                        payStatus,
                        inventoryStatus,
                        createdAtFrom,
                        createdAtTo)))
                .orElse(0L);
    }

    public List<Order> pageOrders(
            Long userId,
            String orderNo,
            String orderStatus,
            String payStatus,
            String inventoryStatus,
            Instant createdAtFrom,
            Instant createdAtTo,
            int offset,
            int limit) {
        Long tenantId = requireTenantId();
        List<OrderDO> pageOrders = orderMapper.selectList(buildPageQuery(
                        tenantId, userId, orderNo, orderStatus, payStatus, inventoryStatus, createdAtFrom, createdAtTo)
                .orderByDesc(OrderDO::getCreatedAt, OrderDO::getId)
                .last("limit " + offset + "," + limit));
        if (pageOrders.isEmpty()) {
            return List.of();
        }
        List<String> orderNos = pageOrders.stream().map(OrderDO::getOrderNo).toList();
        List<Long> orderIds = pageOrders.stream().map(OrderDO::getId).toList();
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
        List<Order> records = pageOrders.stream()
                .map(orderData -> orderPersistenceAssembler.toDomain(
                        orderData, paymentSnapshotMap.get(orderData.getId()), inventorySnapshotMap.get(orderData.getOrderNo())))
                .toList();
        return records;
    }

    public List<Order> findAll() {
        return orderMapper
                .selectList(Wrappers.<OrderDO>lambdaQuery().orderByDesc(OrderDO::getCreatedAt, OrderDO::getId))
                .stream()
                .map(this::toDomainWithSnapshots)
                .toList();
    }

    private com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OrderDO> buildPageQuery(
            Long tenantId,
            Long userId,
            String orderNo,
            String orderStatus,
            String payStatus,
            String inventoryStatus,
            Instant createdAtFrom,
            Instant createdAtTo) {
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

    private OrderId toDomainOrderId(Long orderId) {
        return orderId == null ? null : OrderId.of(orderId);
    }

    private Long requireTenantId() {
        return BaconContextHolder.requireTenantId();
    }
}
