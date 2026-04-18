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
import com.github.thundax.bacon.order.application.query.OrderQueryApplicationService;
import com.github.thundax.bacon.order.application.result.OrderPageResult;
import com.github.thundax.bacon.order.application.saga.OrderOutboxActionExecutor;
import com.github.thundax.bacon.order.application.support.OrderDerivedDataPersistenceSupport;
import com.github.thundax.bacon.order.domain.model.entity.Order;
import com.github.thundax.bacon.order.domain.model.entity.OrderAuditLog;
import com.github.thundax.bacon.order.domain.model.entity.OrderIdempotencyRecord;
import com.github.thundax.bacon.order.domain.model.entity.OrderInventorySnapshot;
import com.github.thundax.bacon.order.domain.model.entity.OrderItem;
import com.github.thundax.bacon.order.domain.model.entity.OrderOutboxEvent;
import com.github.thundax.bacon.order.domain.model.entity.OrderPaymentSnapshot;
import com.github.thundax.bacon.order.domain.model.enums.PayStatus;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderId;
import com.github.thundax.bacon.order.domain.model.valueobject.ReservationNo;
import com.github.thundax.bacon.order.domain.repository.OrderIdempotencyRepository;
import com.github.thundax.bacon.order.domain.repository.OrderOutboxRepository;
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
        OrderCreateApplicationService service = newCreateService(
                repository,
                () -> OrderNo.of("ORD-10001"),
                new SuccessInventoryCommandFacade(),
                new SuccessPaymentCommandFacade());
        OrderQueryApplicationService queryService = new OrderQueryApplicationService(repository);

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
        SuccessInventoryCommandFacade inventoryFacade = new SuccessInventoryCommandFacade();
        OrderCreateApplicationService createService = newCreateService(
                repository, new SequenceOrderNoGenerator(), inventoryFacade, new SuccessPaymentCommandFacade());
        OrderPaymentResultApplicationService paymentResultService = new OrderPaymentResultApplicationService(
                repository,
                inventoryFacade,
                new OrderIdempotencyExecutor(new TestOrderIdempotencyRepository()),
                new OrderDerivedDataPersistenceSupport(repository, new TestIdGenerator()));
        OrderQueryApplicationService queryService = new OrderQueryApplicationService(repository);
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
            Order paidOrder = repository.findByOrderNo(paid.getOrderNo()).orElseThrow();
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
        SuccessInventoryCommandFacade inventoryFacade = new SuccessInventoryCommandFacade();
        SuccessPaymentCommandFacade paymentFacade = new SuccessPaymentCommandFacade();
        OrderCreateApplicationService createService =
                newCreateService(repository, () -> OrderNo.of("ORD-CANCEL-1"), inventoryFacade, paymentFacade);
        OrderCancelApplicationService cancelService = new OrderCancelApplicationService(
                repository,
                inventoryFacade,
                paymentFacade,
                new OrderIdempotencyExecutor(new TestOrderIdempotencyRepository()),
                new OrderDerivedDataPersistenceSupport(repository, new TestIdGenerator()));
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
                .findByOrderNo(created.getOrderNo())
                .orElseThrow());

        assertEquals("CANCELLED", found.getOrderStatus().value());
        assertEquals("SYSTEM_CANCELLED", found.getCancelReason());
        assertNotNull(found.getClosedAt());
    }

    @Test
    void createShouldCloseOrderWhenInventoryReserveFailed() {
        TestOrderRepository repository = new TestOrderRepository();
        OrderCreateApplicationService service = newCreateService(
                repository,
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
        OrderCreateApplicationService service = newCreateService(
                repository, () -> OrderNo.of("ORD-FAIL-2"), inventoryFacade, new FailedPaymentCommandFacade());
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
            TestOrderRepository repository,
            OrderNoGenerator generator,
            InventoryCommandFacade inventoryFacade,
            PaymentCommandFacade paymentFacade) {
        IdGenerator idGenerator = new TestIdGenerator();
        OrderDerivedDataPersistenceSupport support = new OrderDerivedDataPersistenceSupport(repository, idGenerator);
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
            String key = keyOf(valueOf(record.getOrderNo()), record.getEventType());
            OrderIdempotencyRecord value = OrderIdempotencyRecord.reconstruct(
                    com.github.thundax.bacon.order.domain.model.valueobject.OrderIdempotencyRecordKey.of(
                            record.getOrderNo(), record.getEventType()),
                    com.github.thundax.bacon.order.domain.model.enums.OrderIdempotencyStatus.PROCESSING,
                    1,
                    null,
                    record.getProcessingOwner(),
                    record.getLeaseUntil(),
                    record.getClaimedAt(),
                    Instant.now(),
                    Instant.now());
            return storage.putIfAbsent(key, value) == null;
        }

        @Override
        public Optional<OrderIdempotencyRecord> findByKey(
                com.github.thundax.bacon.order.domain.model.valueobject.OrderIdempotencyRecordKey key) {
            return Optional.ofNullable(storage.get(keyOf(key.orderNo().value(), key.eventType())));
        }

        @Override
        public boolean markSuccess(
                com.github.thundax.bacon.order.domain.model.valueobject.OrderIdempotencyRecordKey key,
                Instant updatedAt) {
            AtomicLong updated = new AtomicLong(0);
            storage.computeIfPresent(keyOf(key.orderNo().value(), key.eventType()), (mapKey, existing) -> {
                if (existing.getStatus()
                        != com.github.thundax.bacon.order.domain.model.enums.OrderIdempotencyStatus.PROCESSING) {
                    return existing;
                }
                existing.setStatus(com.github.thundax.bacon.order.domain.model.enums.OrderIdempotencyStatus.SUCCESS);
                existing.setLastError(null);
                existing.setProcessingOwner(null);
                existing.setLeaseUntil(null);
                existing.setClaimedAt(null);
                existing.setUpdatedAt(updatedAt);
                updated.incrementAndGet();
                return existing;
            });
            return updated.get() > 0;
        }

        @Override
        public boolean markFailed(
                com.github.thundax.bacon.order.domain.model.valueobject.OrderIdempotencyRecordKey key,
                String lastError,
                Instant updatedAt) {
            AtomicLong updated = new AtomicLong(0);
            storage.computeIfPresent(keyOf(key.orderNo().value(), key.eventType()), (mapKey, existing) -> {
                if (existing.getStatus()
                        != com.github.thundax.bacon.order.domain.model.enums.OrderIdempotencyStatus.PROCESSING) {
                    return existing;
                }
                existing.setStatus(com.github.thundax.bacon.order.domain.model.enums.OrderIdempotencyStatus.FAILED);
                existing.setLastError(lastError);
                existing.setProcessingOwner(null);
                existing.setLeaseUntil(null);
                existing.setClaimedAt(null);
                existing.setUpdatedAt(updatedAt);
                updated.incrementAndGet();
                return existing;
            });
            return updated.get() > 0;
        }

        @Override
        public boolean recoverFailed(
                com.github.thundax.bacon.order.domain.model.valueobject.OrderIdempotencyRecordKey key,
                String processingOwner,
                Instant leaseUntil,
                Instant claimedAt,
                Instant updatedAt) {
            AtomicLong updated = new AtomicLong(0);
            storage.computeIfPresent(keyOf(key.orderNo().value(), key.eventType()), (mapKey, existing) -> {
                if (existing.getStatus()
                        != com.github.thundax.bacon.order.domain.model.enums.OrderIdempotencyStatus.FAILED) {
                    return existing;
                }
                existing.setStatus(com.github.thundax.bacon.order.domain.model.enums.OrderIdempotencyStatus.PROCESSING);
                existing.setAttemptCount(existing.getAttemptCount() + 1);
                existing.setLastError(null);
                existing.setProcessingOwner(processingOwner);
                existing.setLeaseUntil(leaseUntil);
                existing.setClaimedAt(claimedAt);
                existing.setUpdatedAt(updatedAt);
                updated.incrementAndGet();
                return existing;
            });
            return updated.get() > 0;
        }

        @Override
        public boolean claimExpired(
                com.github.thundax.bacon.order.domain.model.valueobject.OrderIdempotencyRecordKey key,
                String processingOwner,
                Instant leaseUntil,
                Instant claimedAt,
                Instant updatedAt) {
            AtomicLong updated = new AtomicLong(0);
            storage.computeIfPresent(keyOf(key.orderNo().value(), key.eventType()), (mapKey, existing) -> {
                if (existing.getStatus()
                        != com.github.thundax.bacon.order.domain.model.enums.OrderIdempotencyStatus.PROCESSING) {
                    return existing;
                }
                Instant existingLease = existing.getLeaseUntil();
                if (existingLease != null && existingLease.isAfter(claimedAt)) {
                    return existing;
                }
                existing.setProcessingOwner(processingOwner);
                existing.setLeaseUntil(leaseUntil);
                existing.setClaimedAt(claimedAt);
                existing.setUpdatedAt(updatedAt);
                updated.incrementAndGet();
                return existing;
            });
            return updated.get() > 0;
        }

        private String keyOf(String orderNo, String eventType) {
            return orderNo + ":" + eventType;
        }

        private String valueOf(OrderNo orderNo) {
            return orderNo == null ? null : orderNo.value();
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
        private final Map<Long, OrderPaymentSnapshot> paymentSnapshots = new ConcurrentHashMap<>();
        private final Map<Long, Long> paymentSnapshotTenantStorage = new ConcurrentHashMap<>();
        private final Map<String, OrderInventorySnapshot> inventorySnapshots = new ConcurrentHashMap<>();
        private final Map<String, Long> inventorySnapshotTenantStorage = new ConcurrentHashMap<>();
        private final Map<String, List<OrderAuditLog>> auditLogs = new ConcurrentHashMap<>();
        private final AtomicLong idGenerator = new AtomicLong(1000L);

        @Override
        public Order insert(Order order) {
            order.setId(OrderId.of(idGenerator.getAndIncrement()));
            storage.put(toOrderIdValue(order), order);
            orderTenantStorage.put(toOrderIdValue(order), currentTenantId());
            return order;
        }

        @Override
        public Order update(Order order) {
            storage.put(toOrderIdValue(order), order);
            orderTenantStorage.put(toOrderIdValue(order), currentTenantId());
            return order;
        }

        @Override
        public Optional<Order> findById(Long id) {
            if (!isTenantMatched(orderTenantStorage.get(id))) {
                return Optional.empty();
            }
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public Optional<Order> findByOrderNo(String orderNo) {
            return storage.values().stream()
                    .filter(order -> isTenantMatched(orderTenantStorage.get(toOrderIdValue(order))))
                    .filter(order -> orderNo.equals(toOrderNoValue(order.getOrderNo())))
                    .findFirst();
        }

        @Override
        public void updateItems(Long orderId, List<OrderItem> items) {
            itemStorage.put(orderId, items == null ? List.of() : List.copyOf(items));
            itemTenantStorage.put(orderId, currentTenantId());
        }

        @Override
        public List<OrderItem> listItemsByOrderId(Long orderId) {
            if (!isTenantMatched(itemTenantStorage.get(orderId))) {
                return List.of();
            }
            return itemStorage.getOrDefault(orderId, List.of());
        }

        @Override
        public void insertPayment(OrderPaymentSnapshot snapshot) {
            Long orderId = toOrderIdValue(snapshot.getOrderId());
            paymentSnapshots.put(orderId, snapshot);
            paymentSnapshotTenantStorage.put(orderId, currentTenantId());
        }

        @Override
        public void updatePayment(OrderPaymentSnapshot snapshot) {
            Long orderId = toOrderIdValue(snapshot.getOrderId());
            paymentSnapshots.put(orderId, snapshot);
            paymentSnapshotTenantStorage.put(orderId, currentTenantId());
        }

        @Override
        public Optional<OrderPaymentSnapshot> findPaymentByOrderId(Long orderId) {
            OrderPaymentSnapshot snapshot = paymentSnapshots.get(orderId);
            if (snapshot == null || !isTenantMatched(paymentSnapshotTenantStorage.get(orderId))) {
                return Optional.empty();
            }
            return Optional.of(snapshot);
        }

        @Override
        public void insertInventory(OrderInventorySnapshot snapshot) {
            String orderNo = toOrderNoValue(snapshot.getOrderNo());
            inventorySnapshots.put(orderNo, snapshot);
            inventorySnapshotTenantStorage.put(orderNo, currentTenantId());
        }

        @Override
        public void updateInventory(OrderInventorySnapshot snapshot) {
            String orderNo = toOrderNoValue(snapshot.getOrderNo());
            inventorySnapshots.put(orderNo, snapshot);
            inventorySnapshotTenantStorage.put(orderNo, currentTenantId());
        }

        @Override
        public Optional<OrderInventorySnapshot> findInventoryByOrderNo(String orderNo) {
            OrderInventorySnapshot snapshot = inventorySnapshots.get(orderNo);
            if (snapshot == null || !isTenantMatched(inventorySnapshotTenantStorage.get(orderNo))) {
                return Optional.empty();
            }
            return Optional.of(snapshot);
        }

        @Override
        public void insertLog(OrderAuditLog auditLog) {
            String key = currentTenantId() + ":" + toOrderNoValue(auditLog.getOrderNo());
            auditLogs
                    .computeIfAbsent(key, unused -> new java.util.ArrayList<>())
                    .add(auditLog);
        }

        @Override
        public List<OrderAuditLog> listLogs(String orderNo) {
            return List.copyOf(auditLogs.getOrDefault(currentTenantId() + ":" + orderNo, List.of()));
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
            return filterOrders(userId, orderNo, orderStatus, payStatus, inventoryStatus, createdAtFrom, createdAtTo)
                    .size();
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
            return filterOrders(userId, orderNo, orderStatus, payStatus, inventoryStatus, createdAtFrom, createdAtTo)
                    .stream()
                    .skip((long) Math.max(pageNo - 1, 0) * Math.max(pageSize, 1))
                    .limit(Math.max(pageSize, 1))
                    .toList();
        }

        private List<Order> filterOrders(
                Long userId,
                String orderNo,
                String orderStatus,
                String payStatus,
                String inventoryStatus,
                Instant createdAtFrom,
                Instant createdAtTo) {
            List<Order> filtered = storage.values().stream()
                    .filter(order -> isTenantMatched(orderTenantStorage.get(toOrderIdValue(order))))
                    .filter(order -> userId == null || userId.equals(toUserIdValue(order)))
                    .filter(order -> orderNo == null
                            || toOrderNoValue(order.getOrderNo()).contains(orderNo))
                    .filter(order ->
                            orderStatus == null || orderStatus.equals(toOrderStatusValue(order.getOrderStatus())))
                    .filter(order -> payStatus == null || payStatus.equals(toPayStatusValue(order.getPayStatus())))
                    .filter(order -> inventoryStatus == null
                            || inventoryStatus.equals(toInventoryStatusValue(order.getInventoryStatus())))
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

        @Override
        public List<Order> list() {
            return storage.values().stream().toList();
        }

        private Long toOrderIdValue(Order order) {
            return order.getId() == null ? null : order.getId().value();
        }

        private Long toUserIdValue(Order order) {
            return order.getUserId() == null
                    ? null
                    : Long.valueOf(order.getUserId().value());
        }

        private Long toOrderIdValue(OrderId orderId) {
            return orderId == null ? null : orderId.value();
        }

        private String toOrderNoValue(OrderNo orderNo) {
            return orderNo == null ? null : orderNo.value();
        }

        private String toOrderStatusValue(com.github.thundax.bacon.order.domain.model.enums.OrderStatus orderStatus) {
            return orderStatus == null ? null : orderStatus.value();
        }

        private String toPayStatusValue(com.github.thundax.bacon.order.domain.model.enums.PayStatus payStatus) {
            return payStatus == null ? null : payStatus.value();
        }

        private String toInventoryStatusValue(
                com.github.thundax.bacon.order.domain.model.enums.InventoryStatus inventoryStatus) {
            return inventoryStatus == null ? null : inventoryStatus.value();
        }

        private Long currentTenantId() {
            return BaconContextHolder.requireTenantId();
        }

        private boolean isTenantMatched(Long tenantId) {
            return tenantId != null && tenantId.equals(currentTenantId());
        }
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
