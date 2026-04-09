package com.github.thundax.bacon.order.application.command;

import com.github.thundax.bacon.inventory.api.dto.InventoryReservationItemDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationResultDTO;
import com.github.thundax.bacon.inventory.api.facade.InventoryCommandFacade;
import com.github.thundax.bacon.common.id.domain.OrderId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.commerce.valueobject.Money;
import com.github.thundax.bacon.common.commerce.enums.CurrencyCode;
import com.github.thundax.bacon.order.api.dto.OrderSummaryDTO;
import com.github.thundax.bacon.order.api.dto.OrderPageQueryDTO;
import com.github.thundax.bacon.order.api.dto.OrderPageResultDTO;
import com.github.thundax.bacon.order.application.executor.OrderIdempotencyExecutor;
import com.github.thundax.bacon.order.application.query.OrderQueryApplicationService;
import com.github.thundax.bacon.order.application.saga.OrderOutboxActionExecutor;
import com.github.thundax.bacon.order.application.support.OrderDerivedDataPersistenceSupport;
import com.github.thundax.bacon.order.domain.model.entity.OrderIdempotencyRecord;
import com.github.thundax.bacon.order.domain.model.entity.Order;
import com.github.thundax.bacon.order.domain.model.entity.OrderAuditLog;
import com.github.thundax.bacon.order.domain.model.entity.OrderInventorySnapshot;
import com.github.thundax.bacon.order.domain.model.entity.OrderItem;
import com.github.thundax.bacon.order.domain.model.entity.OrderOutboxEvent;
import com.github.thundax.bacon.order.domain.model.entity.OrderPaymentSnapshot;
import com.github.thundax.bacon.common.commerce.valueobject.OrderNo;
import com.github.thundax.bacon.common.commerce.valueobject.PaymentNo;
import com.github.thundax.bacon.order.domain.model.valueobject.ReservationNo;
import com.github.thundax.bacon.common.commerce.valueobject.WarehouseCode;
import com.github.thundax.bacon.order.domain.repository.OrderIdempotencyRepository;
import com.github.thundax.bacon.order.domain.repository.OrderOutboxRepository;
import com.github.thundax.bacon.order.domain.repository.OrderRepository;
import com.github.thundax.bacon.order.domain.service.OrderNoGenerator;
import com.github.thundax.bacon.payment.api.dto.PaymentCloseResultDTO;
import com.github.thundax.bacon.payment.api.dto.PaymentCreateResultDTO;
import com.github.thundax.bacon.payment.api.facade.PaymentCommandFacade;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OrderCreateApplicationServiceTest {

    @Test
    void createShouldGenerateOrderNoInsideModule() {
        TestOrderRepository repository = new TestOrderRepository();
        OrderCreateApplicationService service = newCreateService(repository, () -> OrderNo.of("ORD-10001"),
                new SuccessInventoryCommandFacade(), new SuccessPaymentCommandFacade());
        OrderQueryApplicationService queryService = new OrderQueryApplicationService(repository);

        OrderSummaryDTO result = service.create(new CreateOrderCommand(1001L, 2001L, "CNY", "MOCK", "remark",
                Instant.parse("2026-03-30T00:00:00Z"),
                List.of(new CreateOrderItemCommand(101L, "demo-item", "https://cdn.example.com/101.png", 2,
                        BigDecimal.valueOf(10)))));

        assertEquals("ORD-10001", result.getOrderNo());
        assertEquals(1001L, result.getTenantId());
        assertEquals(new BigDecimal("20.00"), result.getTotalAmount());
        assertEquals("RESERVING_STOCK", result.getOrderStatus());
        assertEquals("UNPAID", result.getPayStatus());
        assertEquals("RESERVING", result.getInventoryStatus());
        assertEquals(1, queryService.getByOrderNo(1001L, "ORD-10001").getItems().size());
        assertEquals("https://cdn.example.com/101.png",
                queryService.getByOrderNo(1001L, "ORD-10001").getItems().get(0).getImageUrl());
    }

    @Test
    void pageOrdersShouldRespectFilterAndPaging() {
        TestOrderRepository repository = new TestOrderRepository();
        SuccessInventoryCommandFacade inventoryFacade = new SuccessInventoryCommandFacade();
        OrderCreateApplicationService createService = newCreateService(repository, new SequenceOrderNoGenerator(),
                inventoryFacade, new SuccessPaymentCommandFacade());
        OrderPaymentResultApplicationService paymentResultService = new OrderPaymentResultApplicationService(repository,
                inventoryFacade, new OrderIdempotencyExecutor(new TestOrderIdempotencyRepository()),
                new OrderDerivedDataPersistenceSupport(repository));
        OrderQueryApplicationService queryService = new OrderQueryApplicationService(repository);
        createService.create(new CreateOrderCommand(1001L, 2001L, "CNY", "MOCK", "r1",
                Instant.parse("2026-03-30T00:00:00Z"),
                List.of(new CreateOrderItemCommand(101L, "item-1", "https://cdn.example.com/101.png", 1,
                        BigDecimal.valueOf(10)))));
        OrderSummaryDTO paid = createService.create(new CreateOrderCommand(1001L, 2001L, "CNY", "MOCK", "r2",
                Instant.parse("2026-03-30T00:00:00Z"),
                List.of(new CreateOrderItemCommand(102L, "item-2", "https://cdn.example.com/102.png", 1,
                        BigDecimal.valueOf(20)))));
        createService.create(new CreateOrderCommand(1002L, 2002L, "CNY", "MOCK", "r3",
                Instant.parse("2026-03-30T00:00:00Z"),
                List.of(new CreateOrderItemCommand(103L, "item-3", "https://cdn.example.com/103.png", 1,
                        BigDecimal.valueOf(30)))));
        Order paidOrder = repository.findByOrderNo(1001L, paid.getOrderNo()).orElseThrow();
        paidOrder.markInventoryReserved(ReservationNo.of("RSV-" + paid.getOrderNo()), WarehouseCode.of("1"));
        paidOrder.markPendingPayment(PaymentNo.of("PAY-" + paid.getOrderNo()), "MOCK");
        repository.save(paidOrder);
        paymentResultService.markPaid(1001L, paid.getOrderNo(), "PAY-1", "MOCK", BigDecimal.valueOf(20),
                Instant.parse("2026-03-26T10:00:00Z"));

        OrderPageResultDTO page = queryService.pageOrders(new OrderPageQueryDTO(1001L, 2001L, null, null, "UNPAID",
                null, null, null, 1, 10));

        assertEquals(1, page.getTotal());
        assertEquals(1, page.getRecords().size());
        assertEquals("UNPAID", page.getRecords().get(0).getPayStatus());
    }

    @Test
    void cancelShouldPersistGivenReason() {
        TestOrderRepository repository = new TestOrderRepository();
        SuccessInventoryCommandFacade inventoryFacade = new SuccessInventoryCommandFacade();
        SuccessPaymentCommandFacade paymentFacade = new SuccessPaymentCommandFacade();
        OrderCreateApplicationService createService = newCreateService(repository, () -> OrderNo.of("ORD-CANCEL-1"),
                inventoryFacade, paymentFacade);
        OrderCancelApplicationService cancelService = new OrderCancelApplicationService(repository, inventoryFacade,
                paymentFacade, new OrderIdempotencyExecutor(new TestOrderIdempotencyRepository()),
                new OrderDerivedDataPersistenceSupport(repository));
        OrderSummaryDTO created = createService.create(new CreateOrderCommand(1001L, 2001L, "CNY", "MOCK", "r1",
                Instant.parse("2026-03-30T00:00:00Z"),
                List.of(new CreateOrderItemCommand(101L, "item-1", "https://cdn.example.com/101.png", 1,
                        BigDecimal.valueOf(10)))));

        cancelService.cancel(1001L, created.getOrderNo(), "SYSTEM_CANCELLED");
        Order found = repository.findByOrderNo(1001L, created.getOrderNo()).orElseThrow();

        assertEquals("CANCELLED", found.getOrderStatusValue());
        assertEquals("SYSTEM_CANCELLED", found.getCancelReason());
        assertNotNull(found.getClosedAt());
    }

    @Test
    void createShouldCloseOrderWhenInventoryReserveFailed() {
        TestOrderRepository repository = new TestOrderRepository();
        OrderCreateApplicationService service = newCreateService(repository, () -> OrderNo.of("ORD-FAIL-1"),
                new FailedInventoryCommandFacade(), new SuccessPaymentCommandFacade());
        OrderSummaryDTO summary = service.create(new CreateOrderCommand(1001L, 2001L, "CNY", "MOCK", "remark",
                Instant.parse("2026-03-30T00:00:00Z"),
                List.of(new CreateOrderItemCommand(101L, "demo-item", "https://cdn.example.com/101.png", 2,
                        BigDecimal.valueOf(10)))));
        assertEquals("RESERVING_STOCK", summary.getOrderStatus());
    }

    @Test
    void createShouldReleaseInventoryWhenPaymentCreateFailed() {
        TrackingInventoryCommandFacade inventoryFacade = new TrackingInventoryCommandFacade();
        TestOrderRepository repository = new TestOrderRepository();
        OrderCreateApplicationService service = newCreateService(repository, () -> OrderNo.of("ORD-FAIL-2"),
                inventoryFacade, new FailedPaymentCommandFacade());
        OrderSummaryDTO summary = service.create(new CreateOrderCommand(1001L, 2001L, "CNY", "MOCK", "remark",
                Instant.parse("2026-03-30T00:00:00Z"),
                List.of(new CreateOrderItemCommand(101L, "demo-item", "https://cdn.example.com/101.png", 2,
                        BigDecimal.valueOf(10)))));
        assertEquals("RESERVING_STOCK", summary.getOrderStatus());
    }

    private OrderCreateApplicationService newCreateService(TestOrderRepository repository, OrderNoGenerator generator,
                                                           InventoryCommandFacade inventoryFacade,
                                                           PaymentCommandFacade paymentFacade) {
        OrderDerivedDataPersistenceSupport support = new OrderDerivedDataPersistenceSupport(repository);
        return new OrderCreateApplicationService(repository, generator,
                new OrderOutboxActionExecutor(repository, new TestOrderOutboxRepository(), inventoryFacade,
                        paymentFacade, support),
                support);
    }

    private static final class TestOrderOutboxRepository implements OrderOutboxRepository {

        @Override
        public void saveOutboxEvent(OrderOutboxEvent event) {
            // no-op for unit tests
        }
    }

    private static final class TestOrderIdempotencyRepository implements OrderIdempotencyRepository {

        private final Map<String, OrderIdempotencyRecord> storage = new ConcurrentHashMap<>();

        @Override
        public boolean createProcessing(OrderIdempotencyRecord record) {
            String key = keyOf(record.getTenantIdValue(), record.getOrderNoValue(), record.getEventType());
            OrderIdempotencyRecord value = new OrderIdempotencyRecord(record.getTenantIdValue(),
                    record.getOrderNoValue(), record.getEventType(),
                    com.github.thundax.bacon.order.domain.model.enums.OrderIdempotencyStatus.PROCESSING, 1, null, record.getProcessingOwner(),
                    record.getLeaseUntil(), record.getClaimedAt(), Instant.now(), Instant.now());
            return storage.putIfAbsent(key, value) == null;
        }

        @Override
        public Optional<OrderIdempotencyRecord> findByBusinessKey(
                com.github.thundax.bacon.order.domain.model.valueobject.OrderIdempotencyRecordKey key) {
            return Optional.ofNullable(storage.get(keyOf(Long.valueOf(key.tenantId().value()), key.orderNo().value(),
                    key.eventType())));
        }

        @Override
        public boolean markSuccess(com.github.thundax.bacon.order.domain.model.valueobject.OrderIdempotencyRecordKey key,
                                   Instant updatedAt) {
            AtomicLong updated = new AtomicLong(0);
            storage.computeIfPresent(keyOf(Long.valueOf(key.tenantId().value()), key.orderNo().value(), key.eventType()),
                    (mapKey, existing) -> {
                if (existing.getStatus() != com.github.thundax.bacon.order.domain.model.enums.OrderIdempotencyStatus.PROCESSING) {
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
        public boolean markFailed(com.github.thundax.bacon.order.domain.model.valueobject.OrderIdempotencyRecordKey key,
                                  String lastError, Instant updatedAt) {
            AtomicLong updated = new AtomicLong(0);
            storage.computeIfPresent(keyOf(Long.valueOf(key.tenantId().value()), key.orderNo().value(), key.eventType()),
                    (mapKey, existing) -> {
                if (existing.getStatus() != com.github.thundax.bacon.order.domain.model.enums.OrderIdempotencyStatus.PROCESSING) {
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
        public boolean retryFromFailed(com.github.thundax.bacon.order.domain.model.valueobject.OrderIdempotencyRecordKey key,
                                       String processingOwner, Instant leaseUntil, Instant claimedAt, Instant updatedAt) {
            AtomicLong updated = new AtomicLong(0);
            storage.computeIfPresent(keyOf(Long.valueOf(key.tenantId().value()), key.orderNo().value(), key.eventType()),
                    (mapKey, existing) -> {
                if (existing.getStatus() != com.github.thundax.bacon.order.domain.model.enums.OrderIdempotencyStatus.FAILED) {
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
        public boolean claimExpiredProcessing(com.github.thundax.bacon.order.domain.model.valueobject.OrderIdempotencyRecordKey key,
                                              String processingOwner, Instant leaseUntil, Instant claimedAt,
                                              Instant updatedAt) {
            AtomicLong updated = new AtomicLong(0);
            storage.computeIfPresent(keyOf(Long.valueOf(key.tenantId().value()), key.orderNo().value(), key.eventType()),
                    (mapKey, existing) -> {
                if (existing.getStatus() != com.github.thundax.bacon.order.domain.model.enums.OrderIdempotencyStatus.PROCESSING) {
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

        private String keyOf(Long tenantId, String orderNo, String eventType) {
            return tenantId + ":" + orderNo + ":" + eventType;
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
        private final Map<Long, List<OrderItem>> itemStorage = new ConcurrentHashMap<>();
        private final Map<Long, OrderPaymentSnapshot> paymentSnapshots = new ConcurrentHashMap<>();
        private final Map<String, OrderInventorySnapshot> inventorySnapshots = new ConcurrentHashMap<>();
        private final Map<String, List<OrderAuditLog>> auditLogs = new ConcurrentHashMap<>();
        private final AtomicLong idGenerator = new AtomicLong(1000L);

        @Override
        public Order save(Order order) {
            if (order.getId() == null) {
                order.setId(OrderId.of(idGenerator.getAndIncrement()));
            }
            storage.put(toOrderIdValue(order), order);
            return order;
        }

        @Override
        public Optional<Order> findById(Long id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public Optional<Order> findByOrderNo(Long tenantId, String orderNo) {
            return storage.values().stream()
                    .filter(order -> tenantId.equals(order.getTenantIdValue()))
                    .filter(order -> orderNo.equals(order.getOrderNoValue()))
                    .findFirst();
        }

        @Override
        public void saveItems(Long tenantId, Long orderId, List<OrderItem> items) {
            itemStorage.put(orderId, items == null ? List.of() : List.copyOf(items));
        }

        @Override
        public List<OrderItem> findItemsByOrderId(Long tenantId, Long orderId, String currencyCode) {
            return itemStorage.getOrDefault(orderId, List.of()).stream()
                    .filter(item -> tenantId.equals(item.getTenantIdValue()))
                    .toList();
        }

        @Override
        public void savePaymentSnapshot(OrderPaymentSnapshot snapshot) {
            paymentSnapshots.put(snapshot.orderIdValue(), snapshot);
        }

        @Override
        public Optional<OrderPaymentSnapshot> findPaymentSnapshotByOrderId(Long tenantId, Long orderId, String currencyCode) {
            OrderPaymentSnapshot snapshot = paymentSnapshots.get(orderId);
            if (snapshot == null || !tenantId.equals(snapshot.tenantIdValue())) {
                return Optional.empty();
            }
            return Optional.of(snapshot);
        }

        @Override
        public void saveInventorySnapshot(OrderInventorySnapshot snapshot) {
            inventorySnapshots.put(snapshot.orderNoValue(), snapshot);
        }

        @Override
        public Optional<OrderInventorySnapshot> findInventorySnapshotByOrderNo(Long tenantId, String orderNo) {
            OrderInventorySnapshot snapshot = inventorySnapshots.get(orderNo);
            if (snapshot == null || !tenantId.equals(snapshot.tenantIdValue())) {
                return Optional.empty();
            }
            return Optional.of(snapshot);
        }

        @Override
        public void saveAuditLog(OrderAuditLog auditLog) {
            String key = toTenantIdValue(auditLog.tenantId()) + ":" + auditLog.orderNo().value();
            auditLogs.computeIfAbsent(key, unused -> new java.util.ArrayList<>()).add(auditLog);
        }

        @Override
        public List<OrderAuditLog> findAuditLogs(Long tenantId, String orderNo) {
            return List.copyOf(auditLogs.getOrDefault(tenantId + ":" + orderNo, List.of()));
        }

        @Override
        public long countOrders(Long tenantId, Long userId, String orderNo, String orderStatus, String payStatus,
                                String inventoryStatus, Instant createdAtFrom, Instant createdAtTo) {
            return filterOrders(tenantId, userId, orderNo, orderStatus, payStatus, inventoryStatus,
                    createdAtFrom, createdAtTo).size();
        }

        @Override
        public List<Order> pageOrders(Long tenantId, Long userId, String orderNo, String orderStatus, String payStatus,
                                      String inventoryStatus, Instant createdAtFrom, Instant createdAtTo,
                                      int offset, int limit) {
            return filterOrders(tenantId, userId, orderNo, orderStatus, payStatus, inventoryStatus,
                    createdAtFrom, createdAtTo).stream()
                    .skip(offset)
                    .limit(limit)
                    .toList();
        }

        private List<Order> filterOrders(Long tenantId, Long userId, String orderNo, String orderStatus, String payStatus,
                                         String inventoryStatus, Instant createdAtFrom, Instant createdAtTo) {
            List<Order> filtered = storage.values().stream()
                    .filter(order -> tenantId == null || tenantId.equals(order.getTenantIdValue()))
                    .filter(order -> userId == null || userId.equals(toUserIdValue(order)))
                    .filter(order -> orderNo == null || order.getOrderNoValue().contains(orderNo))
                    .filter(order -> orderStatus == null || orderStatus.equals(order.getOrderStatusValue()))
                    .filter(order -> payStatus == null || payStatus.equals(order.getPayStatusValue()))
                    .filter(order -> inventoryStatus == null || inventoryStatus.equals(order.getInventoryStatusValue()))
                    .filter(order -> createdAtFrom == null || !order.getCreatedAt().isBefore(createdAtFrom))
                    .filter(order -> createdAtTo == null || !order.getCreatedAt().isAfter(createdAtTo))
                    .sorted(Comparator.comparing(Order::getCreatedAt).reversed()
                            .thenComparing(this::toOrderIdValue, Comparator.reverseOrder()))
                    .toList();
            return filtered;
        }

        @Override
        public List<Order> findAll() {
            return storage.values().stream().toList();
        }

        private Long toOrderIdValue(Order order) {
            return order.getId() == null ? null : order.getId().value();
        }

        private Long toUserIdValue(Order order) {
            return order.getUserId() == null ? null : Long.valueOf(order.getUserId().value());
        }

        private Long toTenantIdValue(TenantId tenantId) {
            return tenantId == null ? null : Long.valueOf(tenantId.value());
        }
    }

    private static class SuccessInventoryCommandFacade implements InventoryCommandFacade {

        @Override
        public InventoryReservationResultDTO reserveStock(Long tenantId, String orderNo,
                                                          List<InventoryReservationItemDTO> items) {
            return new InventoryReservationResultDTO(tenantId, orderNo, "RSV-" + orderNo, "RESERVED", "RESERVED",
                    "DEFAULT", null, null, null, null);
        }

        @Override
        public InventoryReservationResultDTO releaseReservedStock(Long tenantId, String orderNo, String reason) {
            return new InventoryReservationResultDTO(tenantId, orderNo, "RSV-" + orderNo, "RELEASED", "RELEASED",
                    "DEFAULT", null, reason, Instant.now(), null);
        }

        @Override
        public InventoryReservationResultDTO deductReservedStock(Long tenantId, String orderNo) {
            return new InventoryReservationResultDTO(tenantId, orderNo, "RSV-" + orderNo, "DEDUCTED", "DEDUCTED",
                    "DEFAULT", null, null, null, Instant.now());
        }
    }

    private static final class TrackingInventoryCommandFacade extends SuccessInventoryCommandFacade {

        private String lastReleaseReason;

        @Override
        public InventoryReservationResultDTO releaseReservedStock(Long tenantId, String orderNo, String reason) {
            this.lastReleaseReason = reason;
            return super.releaseReservedStock(tenantId, orderNo, reason);
        }
    }

    private static final class FailedInventoryCommandFacade extends SuccessInventoryCommandFacade {

        @Override
        public InventoryReservationResultDTO reserveStock(Long tenantId, String orderNo,
                                                          List<InventoryReservationItemDTO> items) {
            return new InventoryReservationResultDTO(tenantId, orderNo, "RSV-" + orderNo, "FAILED", "FAILED",
                    "DEFAULT", "stock not enough", null, null, null);
        }
    }

    private static class SuccessPaymentCommandFacade implements PaymentCommandFacade {

        @Override
        public PaymentCreateResultDTO createPayment(Long tenantId, String orderNo, Long userId, BigDecimal amount,
                                                    String channelCode, String subject, Instant expiredAt) {
            return new PaymentCreateResultDTO(tenantId, "PAY-" + orderNo, orderNo, channelCode, "PAYING",
                    "mock://pay/" + orderNo, expiredAt, null);
        }

        @Override
        public PaymentCloseResultDTO closePayment(Long tenantId, String paymentNo, String reason) {
            return new PaymentCloseResultDTO(tenantId, paymentNo, null, "CLOSED", "SUCCESS", reason, null);
        }
    }

    private static final class FailedPaymentCommandFacade extends SuccessPaymentCommandFacade {

        @Override
        public PaymentCreateResultDTO createPayment(Long tenantId, String orderNo, Long userId, BigDecimal amount,
                                                    String channelCode, String subject, Instant expiredAt) {
            return new PaymentCreateResultDTO(tenantId, null, orderNo, channelCode, "FAILED", null, expiredAt,
                    "payment channel unavailable");
        }
    }
}
