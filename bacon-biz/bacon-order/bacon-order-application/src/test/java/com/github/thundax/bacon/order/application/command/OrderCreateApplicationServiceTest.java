package com.github.thundax.bacon.order.application.command;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.commerce.valueobject.PaymentNo;
import com.github.thundax.bacon.common.commerce.valueobject.WarehouseCode;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.core.context.BaconContextHolder.BaconContext;
import com.github.thundax.bacon.common.id.core.IdGenerator;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.inventory.api.facade.InventoryCommandFacade;
import com.github.thundax.bacon.inventory.api.request.InventoryDeductFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryReleaseFacadeRequest;
import com.github.thundax.bacon.inventory.api.request.InventoryReserveFacadeRequest;
import com.github.thundax.bacon.inventory.api.response.InventoryReservationFacadeResponse;
import com.github.thundax.bacon.order.application.dto.OrderSummaryDTO;
import com.github.thundax.bacon.order.application.executor.OrderIdempotencyExecutor;
import com.github.thundax.bacon.order.application.codec.OrderIdempotencyRecordKeyCodec;
import com.github.thundax.bacon.order.application.query.OrderQueryApplicationService;
import com.github.thundax.bacon.order.application.result.OrderPageResult;
import com.github.thundax.bacon.order.application.saga.OrderOutboxActionExecutor;
import com.github.thundax.bacon.order.application.support.OrderDerivedDataPersistenceSupport;
import com.github.thundax.bacon.order.domain.model.entity.Order;
import com.github.thundax.bacon.order.domain.model.entity.OrderAuditLog;
import com.github.thundax.bacon.order.domain.model.entity.OrderIdempotencyRecord;
import com.github.thundax.bacon.order.domain.model.entity.OrderItem;
import com.github.thundax.bacon.order.domain.model.entity.OrderOutboxEvent;
import com.github.thundax.bacon.order.domain.model.enums.InventoryStatus;
import com.github.thundax.bacon.order.domain.model.enums.OrderStatus;
import com.github.thundax.bacon.order.domain.model.enums.PayStatus;
import com.github.thundax.bacon.order.domain.model.snapshot.OrderInventorySnapshot;
import com.github.thundax.bacon.order.domain.model.snapshot.OrderPaymentSnapshot;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderId;
import com.github.thundax.bacon.order.domain.model.valueobject.ReservationNo;
import com.github.thundax.bacon.order.domain.repository.OrderAuditLogRepository;
import com.github.thundax.bacon.order.domain.repository.OrderIdempotencyRepository;
import com.github.thundax.bacon.order.domain.repository.OrderInventorySnapshotRepository;
import com.github.thundax.bacon.order.domain.repository.OrderOutboxRepository;
import com.github.thundax.bacon.order.domain.repository.OrderPaymentSnapshotRepository;
import com.github.thundax.bacon.order.domain.repository.OrderRepository;
import com.github.thundax.bacon.order.domain.service.OrderNoGenerator;
import com.github.thundax.bacon.payment.api.facade.PaymentCommandFacade;
import com.github.thundax.bacon.payment.api.request.PaymentCloseFacadeRequest;
import com.github.thundax.bacon.payment.api.request.PaymentCreateFacadeRequest;
import com.github.thundax.bacon.payment.api.response.PaymentCloseFacadeResponse;
import com.github.thundax.bacon.payment.api.response.PaymentCreateFacadeResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class OrderCreateApplicationServiceTest {

    @AfterEach
    void tearDown() {
        BaconContextHolder.clear();
    }

    @Test
    void createShouldGenerateOrderNoInsideModule() {
        TestOrderRepository repository = new TestOrderRepository();
        TestOrderAuditLogRepository auditLogRepository = new TestOrderAuditLogRepository();
        TestOrderInventorySnapshotRepository inventorySnapshotRepository = new TestOrderInventorySnapshotRepository();
        TestOrderPaymentSnapshotRepository paymentSnapshotRepository = new TestOrderPaymentSnapshotRepository();
        OrderCreateApplicationService service = newCreateService(
                auditLogRepository,
                repository,
                inventorySnapshotRepository,
                paymentSnapshotRepository,
                () -> OrderNo.of("ORD-10001"),
                new SuccessInventoryCommandFacade(),
                new SuccessPaymentCommandFacade());
        OrderQueryApplicationService queryService =
                new OrderQueryApplicationService(repository, inventorySnapshotRepository, paymentSnapshotRepository);

        OrderSummaryDTO result = runWithContext(
                1001L,
                2001L,
                () -> service.create(new CreateOrderCommand(
                        UserId.of(2001L),
                        "CNY",
                        "MOCK",
                        "remark",
                        Instant.parse("2026-03-30T00:00:00Z"),
                        List.of(new CreateOrderItemCommand(
                                101L, "demo-item", "https://cdn.example.com/101.png", 2, BigDecimal.valueOf(10))))));

        assertEquals("ORD-10001", result.getOrderNo());
        assertEquals(new BigDecimal("20.00"), result.getTotalAmount());
        assertEquals("RESERVING_STOCK", result.getOrderStatus());
        assertEquals("UNPAID", result.getPayStatus());
        assertEquals("RESERVING", result.getInventoryStatus());
        assertEquals(
                1,
                runWithContext(1001L, 2001L, () -> queryService.getByOrderNo(OrderNo.of("ORD-10001")))
                        .getItems()
                        .size());
        assertEquals(
                "https://cdn.example.com/101.png",
                runWithContext(1001L, 2001L, () -> queryService.getByOrderNo(OrderNo.of("ORD-10001")))
                        .getItems()
                        .get(0)
                        .getImageUrl());
    }

    @Test
    void pageOrdersShouldRespectFilterAndPaging() {
        TestOrderRepository repository = new TestOrderRepository();
        TestOrderAuditLogRepository auditLogRepository = new TestOrderAuditLogRepository();
        TestOrderInventorySnapshotRepository inventorySnapshotRepository = new TestOrderInventorySnapshotRepository();
        TestOrderPaymentSnapshotRepository paymentSnapshotRepository = new TestOrderPaymentSnapshotRepository();
        SuccessInventoryCommandFacade inventoryFacade = new SuccessInventoryCommandFacade();
        OrderCreateApplicationService createService = newCreateService(
                auditLogRepository,
                repository,
                inventorySnapshotRepository,
                paymentSnapshotRepository,
                new SequenceOrderNoGenerator(),
                inventoryFacade,
                new SuccessPaymentCommandFacade());
        OrderPaymentResultApplicationService paymentResultService = new OrderPaymentResultApplicationService(
                repository,
                inventoryFacade,
                new OrderIdempotencyExecutor(new TestOrderIdempotencyRepository()),
                new OrderDerivedDataPersistenceSupport(
                        auditLogRepository,
                        repository,
                        inventorySnapshotRepository,
                        paymentSnapshotRepository,
                        new TestIdGenerator()));
        OrderQueryApplicationService queryService =
                new OrderQueryApplicationService(repository, inventorySnapshotRepository, paymentSnapshotRepository);
        runWithContext(
                1001L,
                2001L,
                () -> createService.create(new CreateOrderCommand(
                        UserId.of(2001L),
                        "CNY",
                        "MOCK",
                        "r1",
                        Instant.parse("2026-03-30T00:00:00Z"),
                        List.of(new CreateOrderItemCommand(
                                101L, "item-1", "https://cdn.example.com/101.png", 1, BigDecimal.valueOf(10))))));
        OrderSummaryDTO paid = runWithContext(
                1001L,
                2001L,
                () -> createService.create(new CreateOrderCommand(
                        UserId.of(2001L),
                        "CNY",
                        "MOCK",
                        "r2",
                        Instant.parse("2026-03-30T00:00:00Z"),
                        List.of(new CreateOrderItemCommand(
                                102L, "item-2", "https://cdn.example.com/102.png", 1, BigDecimal.valueOf(20))))));
        runWithContext(
                1002L,
                2002L,
                () -> createService.create(new CreateOrderCommand(
                        UserId.of(2002L),
                        "CNY",
                        "MOCK",
                        "r3",
                        Instant.parse("2026-03-30T00:00:00Z"),
                        List.of(new CreateOrderItemCommand(
                                103L, "item-3", "https://cdn.example.com/103.png", 1, BigDecimal.valueOf(30))))));
        runWithContext(1001L, 2001L, () -> {
            Order paidOrder = repository.findByOrderNo(OrderNo.of(paid.getOrderNo())).orElseThrow();
            paidOrder.markInventoryReserved(ReservationNo.of("RSV-" + paid.getOrderNo()), WarehouseCode.of("1"));
            paidOrder.markPendingPayment(PaymentNo.of("PAY-" + paid.getOrderNo()), "MOCK");
            repository.update(paidOrder);
            paymentResultService.markPaid(
                    OrderNo.of(paid.getOrderNo()),
                    PaymentNo.of("PAY-1"),
                    "MOCK",
                    BigDecimal.valueOf(20),
                    Instant.parse("2026-03-26T10:00:00Z"));
        });

        OrderPageResult page = runWithContext(
                1001L,
                2001L,
                () -> queryService.page(UserId.of(2001L), null, null, PayStatus.UNPAID, null, null, null, 1, 10));

        assertEquals(1, page.getTotal());
        assertEquals(1, page.getRecords().size());
        assertEquals("UNPAID", page.getRecords().get(0).getPayStatus());
    }

    @Test
    void cancelShouldPersistGivenReason() {
        TestOrderRepository repository = new TestOrderRepository();
        TestOrderAuditLogRepository auditLogRepository = new TestOrderAuditLogRepository();
        TestOrderInventorySnapshotRepository inventorySnapshotRepository = new TestOrderInventorySnapshotRepository();
        TestOrderPaymentSnapshotRepository paymentSnapshotRepository = new TestOrderPaymentSnapshotRepository();
        SuccessInventoryCommandFacade inventoryFacade = new SuccessInventoryCommandFacade();
        SuccessPaymentCommandFacade paymentFacade = new SuccessPaymentCommandFacade();
        OrderCreateApplicationService createService =
                newCreateService(
                        auditLogRepository,
                        repository,
                        inventorySnapshotRepository,
                        paymentSnapshotRepository,
                        () -> OrderNo.of("ORD-CANCEL-1"),
                        inventoryFacade,
                        paymentFacade);
        OrderCancelApplicationService cancelService = new OrderCancelApplicationService(
                repository,
                inventoryFacade,
                paymentFacade,
                new OrderIdempotencyExecutor(new TestOrderIdempotencyRepository()),
                new OrderDerivedDataPersistenceSupport(
                        auditLogRepository,
                        repository,
                        inventorySnapshotRepository,
                        paymentSnapshotRepository,
                        new TestIdGenerator()));
        OrderSummaryDTO created = runWithContext(
                1001L,
                2001L,
                () -> createService.create(new CreateOrderCommand(
                        UserId.of(2001L),
                        "CNY",
                        "MOCK",
                        "r1",
                        Instant.parse("2026-03-30T00:00:00Z"),
                        List.of(new CreateOrderItemCommand(
                                101L, "item-1", "https://cdn.example.com/101.png", 1, BigDecimal.valueOf(10))))));

        runWithContext(1001L, 2001L, () -> cancelService.cancel(OrderNo.of(created.getOrderNo()), "SYSTEM_CANCELLED"));
        Order found = runWithContext(1001L, 2001L, () -> repository
                .findByOrderNo(OrderNo.of(created.getOrderNo()))
                .orElseThrow());

        assertEquals("CANCELLED", found.getOrderStatus().value());
        assertEquals("SYSTEM_CANCELLED", found.getCancelReason());
        assertNotNull(found.getClosedAt());
    }

    @Test
    void createShouldCloseOrderWhenInventoryReserveFailed() {
        TestOrderRepository repository = new TestOrderRepository();
        TestOrderAuditLogRepository auditLogRepository = new TestOrderAuditLogRepository();
        TestOrderInventorySnapshotRepository inventorySnapshotRepository = new TestOrderInventorySnapshotRepository();
        TestOrderPaymentSnapshotRepository paymentSnapshotRepository = new TestOrderPaymentSnapshotRepository();
        OrderCreateApplicationService service = newCreateService(
                auditLogRepository,
                repository,
                inventorySnapshotRepository,
                paymentSnapshotRepository,
                () -> OrderNo.of("ORD-FAIL-1"),
                new FailedInventoryCommandFacade(),
                new SuccessPaymentCommandFacade());
        OrderSummaryDTO summary = runWithContext(
                1001L,
                2001L,
                () -> service.create(new CreateOrderCommand(
                        UserId.of(2001L),
                        "CNY",
                        "MOCK",
                        "remark",
                        Instant.parse("2026-03-30T00:00:00Z"),
                        List.of(new CreateOrderItemCommand(
                                101L, "demo-item", "https://cdn.example.com/101.png", 2, BigDecimal.valueOf(10))))));
        assertEquals("RESERVING_STOCK", summary.getOrderStatus());
    }

    @Test
    void createShouldReleaseInventoryWhenPaymentCreateFailed() {
        TrackingInventoryCommandFacade inventoryFacade = new TrackingInventoryCommandFacade();
        TestOrderRepository repository = new TestOrderRepository();
        TestOrderAuditLogRepository auditLogRepository = new TestOrderAuditLogRepository();
        TestOrderInventorySnapshotRepository inventorySnapshotRepository = new TestOrderInventorySnapshotRepository();
        TestOrderPaymentSnapshotRepository paymentSnapshotRepository = new TestOrderPaymentSnapshotRepository();
        OrderCreateApplicationService service = newCreateService(
                auditLogRepository,
                repository,
                inventorySnapshotRepository,
                paymentSnapshotRepository,
                () -> OrderNo.of("ORD-FAIL-2"),
                inventoryFacade,
                new FailedPaymentCommandFacade());
        OrderSummaryDTO summary = runWithContext(
                1001L,
                2001L,
                () -> service.create(new CreateOrderCommand(
                        UserId.of(2001L),
                        "CNY",
                        "MOCK",
                        "remark",
                        Instant.parse("2026-03-30T00:00:00Z"),
                        List.of(new CreateOrderItemCommand(
                                101L, "demo-item", "https://cdn.example.com/101.png", 2, BigDecimal.valueOf(10))))));
        assertEquals("RESERVING_STOCK", summary.getOrderStatus());
    }

    private OrderCreateApplicationService newCreateService(
            TestOrderAuditLogRepository auditLogRepository,
            TestOrderRepository repository,
            TestOrderInventorySnapshotRepository inventorySnapshotRepository,
            TestOrderPaymentSnapshotRepository paymentSnapshotRepository,
            OrderNoGenerator generator,
            InventoryCommandFacade inventoryFacade,
            PaymentCommandFacade paymentFacade) {
        IdGenerator idGenerator = new TestIdGenerator();
        OrderDerivedDataPersistenceSupport support = new OrderDerivedDataPersistenceSupport(
                auditLogRepository, repository, inventorySnapshotRepository, paymentSnapshotRepository, idGenerator);
        return new OrderCreateApplicationService(
                repository,
                generator,
                new OrderOutboxActionExecutor(
                        repository, new TestOrderOutboxRepository(), inventoryFacade, paymentFacade, support),
                support,
                idGenerator);
    }

    private static void runWithContext(Long tenantId, Long userId, Runnable action) {
        runWithContext(tenantId, userId, () -> {
            action.run();
            return null;
        });
    }

    private static <T> T runWithContext(Long tenantId, Long userId, Supplier<T> action) {
        BaconContext previous = BaconContextHolder.snapshot();
        try {
            BaconContextHolder.set(new BaconContext(tenantId, userId));
            return action.get();
        } finally {
            BaconContextHolder.restore(previous);
        }
    }

    private static final class TestOrderOutboxRepository implements OrderOutboxRepository {

        @Override
        public void insert(OrderOutboxEvent event) {
            // no-op for unit tests
        }
    }

    private static final class TestOrderIdempotencyRepository implements OrderIdempotencyRepository {

        private final Map<String, OrderIdempotencyRecord> storage = new ConcurrentHashMap<>();

        @Override
        public boolean insert(OrderIdempotencyRecord record) {
            String key = keyOf(
                    OrderIdempotencyRecordKeyCodec.toOrderNoValue(record.getKey()),
                    OrderIdempotencyRecordKeyCodec.toEventTypeValue(record.getKey()));
            OrderIdempotencyRecord value = OrderIdempotencyRecord.reconstruct(
                    record.getKey(),
                    record.getStatus(),
                    record.getAttemptCount(),
                    record.getLastError(),
                    record.getProcessingOwner(),
                    record.getLeaseUntil(),
                    record.getClaimedAt(),
                    record.getCreatedAt(),
                    record.getUpdatedAt());
            return storage.putIfAbsent(key, value) == null;
        }

        @Override
        public Optional<OrderIdempotencyRecord> findByKey(
                com.github.thundax.bacon.order.domain.model.valueobject.OrderIdempotencyRecordKey key) {
            return Optional.ofNullable(storage.get(keyOf(key.orderNo().value(), key.eventType())))
                    .map(existing -> OrderIdempotencyRecord.reconstruct(
                            existing.getKey(),
                            existing.getStatus(),
                            existing.getAttemptCount(),
                            existing.getLastError(),
                            existing.getProcessingOwner(),
                            existing.getLeaseUntil(),
                            existing.getClaimedAt(),
                            existing.getCreatedAt(),
                            existing.getUpdatedAt()));
        }

        @Override
        public boolean updateStatus(
                OrderIdempotencyRecord record,
                com.github.thundax.bacon.order.domain.model.enums.OrderIdempotencyStatus currentStatus) {
            AtomicLong updated = new AtomicLong(0);
            storage.computeIfPresent(keyOf(record.getKey().orderNo().value(), record.getKey().eventType()), (mapKey, existing) -> {
                if (existing.getStatus() != currentStatus) {
                    return existing;
                }
                updated.incrementAndGet();
                return OrderIdempotencyRecord.reconstruct(
                        record.getKey(),
                        record.getStatus(),
                        record.getAttemptCount(),
                        record.getLastError(),
                        record.getProcessingOwner(),
                        record.getLeaseUntil(),
                        record.getClaimedAt(),
                        record.getCreatedAt(),
                        record.getUpdatedAt());
            });
            return updated.get() > 0;
        }

        @Override
        public boolean updateStatus(
                OrderIdempotencyRecord record,
                com.github.thundax.bacon.order.domain.model.enums.OrderIdempotencyStatus currentStatus,
                Instant leaseExpiredBefore) {
            AtomicLong updated = new AtomicLong(0);
            storage.computeIfPresent(keyOf(record.getKey().orderNo().value(), record.getKey().eventType()), (mapKey, existing) -> {
                if (existing.getStatus() != currentStatus) {
                    return existing;
                }
                Instant existingLease = existing.getLeaseUntil();
                if (leaseExpiredBefore != null && existingLease != null && existingLease.isAfter(leaseExpiredBefore)) {
                    return existing;
                }
                updated.incrementAndGet();
                return OrderIdempotencyRecord.reconstruct(
                        record.getKey(),
                        record.getStatus(),
                        record.getAttemptCount(),
                        record.getLastError(),
                        record.getProcessingOwner(),
                        record.getLeaseUntil(),
                        record.getClaimedAt(),
                        record.getCreatedAt(),
                        record.getUpdatedAt());
            });
            return updated.get() > 0;
        }

        @Override
        public List<OrderIdempotencyRecord> listExpiredProcessing(Instant now) {
            return storage.values().stream()
                    .filter(existing -> existing.getStatus()
                            == com.github.thundax.bacon.order.domain.model.enums.OrderIdempotencyStatus.PROCESSING)
                    .filter(existing -> existing.getLeaseUntil() != null && !existing.getLeaseUntil().isAfter(now))
                    .map(existing -> OrderIdempotencyRecord.reconstruct(
                            existing.getKey(),
                            existing.getStatus(),
                            existing.getAttemptCount(),
                            existing.getLastError(),
                            existing.getProcessingOwner(),
                            existing.getLeaseUntil(),
                            existing.getClaimedAt(),
                            existing.getCreatedAt(),
                            existing.getUpdatedAt()))
                    .toList();
        }

        private String keyOf(String orderNo, String eventType) {
            return orderNo + ":" + eventType;
        }

    }

    private static final class TestIdGenerator implements IdGenerator {

        private final AtomicLong sequence = new AtomicLong(1L);

        @Override
        public long nextId(String bizTag) {
            return sequence.getAndIncrement();
        }
    }

    private static final class SequenceOrderNoGenerator implements OrderNoGenerator {

        private final AtomicLong seq = new AtomicLong(10000);

        @Override
        public OrderNo nextOrderNo() {
            return OrderNo.of("ORD-" + seq.incrementAndGet());
        }
    }

    private static final class TestOrderRepository implements OrderRepository {

        private final Map<Long, Order> storage = new ConcurrentHashMap<>();
        private final Map<Long, Long> orderTenantStorage = new ConcurrentHashMap<>();
        private final Map<Long, List<OrderItem>> itemStorage = new ConcurrentHashMap<>();
        private final Map<Long, Long> itemTenantStorage = new ConcurrentHashMap<>();
        private final AtomicLong idGenerator = new AtomicLong(1000L);

        @Override
        public Order insert(Order order) {
            Order savedOrder = withId(order, OrderId.of(idGenerator.getAndIncrement()));
            storage.put(toOrderIdValue(savedOrder), savedOrder);
            orderTenantStorage.put(toOrderIdValue(savedOrder), currentTenantId());
            return savedOrder;
        }

        @Override
        public Order update(Order order) {
            storage.put(toOrderIdValue(order), order);
            orderTenantStorage.put(toOrderIdValue(order), currentTenantId());
            return order;
        }

        @Override
        public Optional<Order> findById(OrderId id) {
            Long orderId = toOrderIdValue(id);
            if (!isTenantMatched(orderTenantStorage.get(orderId))) {
                return Optional.empty();
            }
            return Optional.ofNullable(storage.get(orderId));
        }

        @Override
        public Optional<Order> findByOrderNo(OrderNo orderNo) {
            return storage.values().stream()
                    .filter(order -> isTenantMatched(orderTenantStorage.get(toOrderIdValue(order))))
                    .filter(order -> toOrderNoValue(orderNo).equals(toOrderNoValue(order.getOrderNo())))
                    .findFirst();
        }

        @Override
        public void updateItems(OrderId orderId, List<OrderItem> items) {
            Long orderIdValue = toOrderIdValue(orderId);
            itemStorage.put(orderIdValue, items == null ? List.of() : List.copyOf(items));
            itemTenantStorage.put(orderIdValue, currentTenantId());
        }

        @Override
        public List<OrderItem> listItemsByOrderId(OrderId orderId) {
            Long orderIdValue = toOrderIdValue(orderId);
            if (!isTenantMatched(itemTenantStorage.get(orderIdValue))) {
                return List.of();
            }
            return itemStorage.getOrDefault(orderIdValue, List.of());
        }

        @Override
        public List<Order> list() {
            return storage.values().stream().toList();
        }

        @Override
        public long count(
                UserId userId,
                OrderNo orderNo,
                OrderStatus orderStatus,
                PayStatus payStatus,
                InventoryStatus inventoryStatus,
                Instant createdAtFrom,
                Instant createdAtTo) {
            return filterOrders(userId, orderNo, orderStatus, payStatus, inventoryStatus, createdAtFrom, createdAtTo)
                    .size();
        }

        @Override
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
            return filterOrders(userId, orderNo, orderStatus, payStatus, inventoryStatus, createdAtFrom, createdAtTo)
                    .stream()
                    .skip((long) Math.max(pageNo - 1, 0) * Math.max(pageSize, 1))
                    .limit(Math.max(pageSize, 1))
                    .toList();
        }

        private List<Order> filterOrders(
                UserId userId,
                OrderNo orderNo,
                OrderStatus orderStatus,
                PayStatus payStatus,
                InventoryStatus inventoryStatus,
                Instant createdAtFrom,
                Instant createdAtTo) {
            List<Order> filtered = storage.values().stream()
                    .filter(order -> isTenantMatched(orderTenantStorage.get(toOrderIdValue(order))))
                    .filter(order -> userId == null || toUserIdValue(userId).equals(toUserIdValue(order)))
                    .filter(order -> orderNo == null
                            || toOrderNoValue(order.getOrderNo()).contains(toOrderNoValue(orderNo)))
                    .filter(order -> orderStatus == null || orderStatus == order.getOrderStatus())
                    .filter(order -> payStatus == null || payStatus == order.getPayStatus())
                    .filter(order -> inventoryStatus == null || inventoryStatus == order.getInventoryStatus())
                    .filter(order ->
                            createdAtFrom == null || !order.getCreatedAt().isBefore(createdAtFrom))
                    .filter(order ->
                            createdAtTo == null || !order.getCreatedAt().isAfter(createdAtTo))
                    .sorted(Comparator.comparing(Order::getCreatedAt)
                            .reversed()
                            .thenComparing(this::toOrderIdValue, Comparator.reverseOrder()))
                    .toList();
            return filtered;
        }

        private Long toOrderIdValue(Order order) {
            return order.getId() == null ? null : order.getId().value();
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

        private Long toUserIdValue(Order order) {
            return order.getUserId() == null
                    ? null
                    : Long.valueOf(order.getUserId().value());
        }

        private Long toUserIdValue(UserId userId) {
            return userId == null ? null : Long.valueOf(userId.value());
        }

        private Long toOrderIdValue(OrderId orderId) {
            return orderId == null ? null : orderId.value();
        }

        private String toOrderNoValue(OrderNo orderNo) {
            return orderNo == null ? null : orderNo.value();
        }

        private Long currentTenantId() {
            return BaconContextHolder.requireTenantId();
        }

        private boolean isTenantMatched(Long tenantId) {
            return tenantId != null && tenantId.equals(currentTenantId());
        }
    }

    private static final class TestOrderPaymentSnapshotRepository implements OrderPaymentSnapshotRepository {

        private final Map<Long, OrderPaymentSnapshot> paymentSnapshots = new ConcurrentHashMap<>();
        private final Map<Long, Long> paymentSnapshotTenantStorage = new ConcurrentHashMap<>();

        @Override
        public void insert(OrderPaymentSnapshot snapshot) {
            Long orderId = toOrderIdValue(snapshot.orderId());
            paymentSnapshots.put(orderId, snapshot);
            paymentSnapshotTenantStorage.put(orderId, currentTenantId());
        }

        @Override
        public void update(OrderPaymentSnapshot snapshot) {
            Long orderId = toOrderIdValue(snapshot.orderId());
            paymentSnapshots.put(orderId, snapshot);
            paymentSnapshotTenantStorage.put(orderId, currentTenantId());
        }

        @Override
        public Optional<OrderPaymentSnapshot> findByOrderId(OrderId orderId) {
            Long orderIdValue = toOrderIdValue(orderId);
            OrderPaymentSnapshot snapshot = paymentSnapshots.get(orderIdValue);
            if (snapshot == null || !isTenantMatched(paymentSnapshotTenantStorage.get(orderIdValue))) {
                return Optional.empty();
            }
            return Optional.of(snapshot);
        }
    }

    private static final class TestOrderInventorySnapshotRepository implements OrderInventorySnapshotRepository {

        private final Map<String, OrderInventorySnapshot> inventorySnapshots = new ConcurrentHashMap<>();
        private final Map<String, Long> inventorySnapshotTenantStorage = new ConcurrentHashMap<>();

        @Override
        public void insert(OrderInventorySnapshot snapshot) {
            String orderNo = toOrderNoValue(snapshot.orderNo());
            inventorySnapshots.put(orderNo, snapshot);
            inventorySnapshotTenantStorage.put(orderNo, currentTenantId());
        }

        @Override
        public void update(OrderInventorySnapshot snapshot) {
            String orderNo = toOrderNoValue(snapshot.orderNo());
            inventorySnapshots.put(orderNo, snapshot);
            inventorySnapshotTenantStorage.put(orderNo, currentTenantId());
        }

        @Override
        public Optional<OrderInventorySnapshot> findByOrderNo(OrderNo orderNo) {
            String orderNoValue = toOrderNoValue(orderNo);
            OrderInventorySnapshot snapshot = inventorySnapshots.get(orderNoValue);
            if (snapshot == null || !isTenantMatched(inventorySnapshotTenantStorage.get(orderNoValue))) {
                return Optional.empty();
            }
            return Optional.of(snapshot);
        }
    }

    private static final class TestOrderAuditLogRepository implements OrderAuditLogRepository {

        private final Map<String, List<OrderAuditLog>> auditLogs = new ConcurrentHashMap<>();

        @Override
        public void insert(OrderAuditLog auditLog) {
            String key = currentTenantId() + ":" + toOrderNoValue(auditLog.getOrderNo());
            auditLogs
                    .computeIfAbsent(key, unused -> new java.util.ArrayList<>())
                    .add(auditLog);
        }

        @Override
        public List<OrderAuditLog> listByOrderNo(OrderNo orderNo) {
            return List.copyOf(auditLogs.getOrDefault(currentTenantId() + ":" + toOrderNoValue(orderNo), List.of()));
        }
    }

    private static Long currentTenantId() {
        return BaconContextHolder.requireTenantId();
    }

    private static boolean isTenantMatched(Long tenantId) {
        return tenantId != null && tenantId.equals(currentTenantId());
    }

    private static Long toOrderIdValue(OrderId orderId) {
        return orderId == null ? null : orderId.value();
    }

    private static String toOrderNoValue(OrderNo orderNo) {
        return orderNo == null ? null : orderNo.value();
    }

    private static class SuccessInventoryCommandFacade implements InventoryCommandFacade {

        @Override
        public InventoryReservationFacadeResponse reserveStock(InventoryReserveFacadeRequest command) {
            return reservationResult(command.getOrderNo(), "RESERVED", null, null, null, null);
        }

        @Override
        public InventoryReservationFacadeResponse releaseReservedStock(InventoryReleaseFacadeRequest command) {
            return reservationResult(command.getOrderNo(), "RELEASED", null, command.getReason(), Instant.now(), null);
        }

        @Override
        public InventoryReservationFacadeResponse deductReservedStock(InventoryDeductFacadeRequest command) {
            return reservationResult(command.getOrderNo(), "DEDUCTED", null, null, null, Instant.now());
        }

        protected final InventoryReservationFacadeResponse reservationResult(
                String orderNo,
                String reservationStatus,
                String failureReason,
                String releaseReason,
                Instant releasedAt,
                Instant deductedAt) {
            return new InventoryReservationFacadeResponse(
                    orderNo,
                    "RSV-" + orderNo,
                    reservationStatus,
                    reservationStatus,
                    "DEFAULT",
                    null,
                    failureReason,
                    releaseReason,
                    null,
                    releasedAt,
                    deductedAt);
        }
    }

    private static final class TrackingInventoryCommandFacade extends SuccessInventoryCommandFacade {

        private String lastReleaseReason;

        @Override
        public InventoryReservationFacadeResponse releaseReservedStock(InventoryReleaseFacadeRequest command) {
            this.lastReleaseReason = command.getReason();
            return super.releaseReservedStock(command);
        }
    }

    private static final class FailedInventoryCommandFacade extends SuccessInventoryCommandFacade {

        @Override
        public InventoryReservationFacadeResponse reserveStock(InventoryReserveFacadeRequest command) {
            return reservationResult(command.getOrderNo(), "FAILED", "stock not enough", null, null, null);
        }
    }

    private static class SuccessPaymentCommandFacade implements PaymentCommandFacade {

        @Override
        public PaymentCreateFacadeResponse createPayment(PaymentCreateFacadeRequest request) {
            return new PaymentCreateFacadeResponse(
                    "PAY-" + request.getOrderNo(),
                    request.getOrderNo(),
                    request.getChannelCode(),
                    "PAYING",
                    "mock://pay/" + request.getOrderNo(),
                    request.getExpiredAt(),
                    null);
        }

        @Override
        public PaymentCloseFacadeResponse closePayment(PaymentCloseFacadeRequest request) {
            return new PaymentCloseFacadeResponse(
                    request.getPaymentNo(), null, "CLOSED", "SUCCESS", request.getReason(), null);
        }
    }

    private static final class FailedPaymentCommandFacade extends SuccessPaymentCommandFacade {

        @Override
        public PaymentCreateFacadeResponse createPayment(PaymentCreateFacadeRequest request) {
            return new PaymentCreateFacadeResponse(
                    null,
                    request.getOrderNo(),
                    request.getChannelCode(),
                    "FAILED",
                    null,
                    request.getExpiredAt(),
                    "payment channel unavailable");
        }
    }
}
