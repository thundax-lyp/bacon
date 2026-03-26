package com.github.thundax.bacon.order.application.service;

import com.github.thundax.bacon.inventory.api.dto.InventoryReservationItemDTO;
import com.github.thundax.bacon.inventory.api.dto.InventoryReservationResultDTO;
import com.github.thundax.bacon.inventory.api.facade.InventoryCommandFacade;
import com.github.thundax.bacon.order.api.dto.OrderSummaryDTO;
import com.github.thundax.bacon.order.api.dto.OrderPageQueryDTO;
import com.github.thundax.bacon.order.api.dto.OrderPageResultDTO;
import com.github.thundax.bacon.order.application.command.CreateOrderCommand;
import com.github.thundax.bacon.order.application.command.CreateOrderItemCommand;
import com.github.thundax.bacon.order.domain.model.entity.Order;
import com.github.thundax.bacon.order.domain.model.entity.OrderAuditLog;
import com.github.thundax.bacon.order.domain.model.entity.OrderInventorySnapshot;
import com.github.thundax.bacon.order.domain.model.entity.OrderItem;
import com.github.thundax.bacon.order.domain.model.entity.OrderPaymentSnapshot;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OrderApplicationServiceTest {

    @Test
    void createShouldGenerateOrderNoInsideModule() {
        OrderApplicationService service = new OrderApplicationService(new TestOrderRepository(), () -> "ORD-10001",
                new SuccessInventoryCommandFacade(), new SuccessPaymentCommandFacade());

        OrderSummaryDTO result = service.create(new CreateOrderCommand(1001L, 2001L, "CNY", "MOCK", "remark",
                Instant.parse("2026-03-30T00:00:00Z"),
                List.of(new CreateOrderItemCommand(101L, "demo-item", 2, BigDecimal.valueOf(10)))));

        assertEquals("ORD-10001", result.getOrderNo());
        assertEquals(1001L, result.getTenantId());
        assertEquals(BigDecimal.valueOf(20), result.getTotalAmount());
        assertEquals("PENDING_PAYMENT", result.getOrderStatus());
        assertEquals("PAYING", result.getPayStatus());
        assertEquals("RESERVED", result.getInventoryStatus());
        assertEquals(1, service.getByOrderNo(1001L, "ORD-10001").getItems().size());
    }

    @Test
    void pageOrdersShouldRespectFilterAndPaging() {
        TestOrderRepository repository = new TestOrderRepository();
        OrderApplicationService service = new OrderApplicationService(repository, new SequenceOrderNoGenerator(),
                new SuccessInventoryCommandFacade(), new SuccessPaymentCommandFacade());
        service.create(new CreateOrderCommand(1001L, 2001L, "CNY", "MOCK", "r1",
                Instant.parse("2026-03-30T00:00:00Z"),
                List.of(new CreateOrderItemCommand(101L, "item-1", 1, BigDecimal.valueOf(10)))));
        OrderSummaryDTO paid = service.create(new CreateOrderCommand(1001L, 2001L, "CNY", "MOCK", "r2",
                Instant.parse("2026-03-30T00:00:00Z"),
                List.of(new CreateOrderItemCommand(102L, "item-2", 1, BigDecimal.valueOf(20)))));
        service.create(new CreateOrderCommand(1002L, 2002L, "CNY", "MOCK", "r3",
                Instant.parse("2026-03-30T00:00:00Z"),
                List.of(new CreateOrderItemCommand(103L, "item-3", 1, BigDecimal.valueOf(30)))));
        service.markPaid(1001L, paid.getOrderNo(), "PAY-1", "MOCK", BigDecimal.valueOf(20),
                Instant.parse("2026-03-26T10:00:00Z"));

        OrderPageResultDTO page = service.pageOrders(new OrderPageQueryDTO(1001L, 2001L, null, null, "PAYING",
                null, null, null, 1, 10));

        assertEquals(1, page.getTotal());
        assertEquals(1, page.getRecords().size());
        assertEquals("PAYING", page.getRecords().get(0).getPayStatus());
    }

    @Test
    void cancelShouldPersistGivenReason() {
        TestOrderRepository repository = new TestOrderRepository();
        OrderApplicationService service = new OrderApplicationService(repository, () -> "ORD-CANCEL-1",
                new SuccessInventoryCommandFacade(), new SuccessPaymentCommandFacade());
        OrderCancelApplicationService cancelService = new OrderCancelApplicationService(service);
        OrderSummaryDTO created = service.create(new CreateOrderCommand(1001L, 2001L, "CNY", "MOCK", "r1",
                Instant.parse("2026-03-30T00:00:00Z"),
                List.of(new CreateOrderItemCommand(101L, "item-1", 1, BigDecimal.valueOf(10)))));

        cancelService.cancel(1001L, created.getOrderNo(), "SYSTEM_CANCELLED");
        Order found = repository.findByOrderNo(1001L, created.getOrderNo()).orElseThrow();

        assertEquals("CANCELLED", found.getOrderStatus());
        assertEquals("SYSTEM_CANCELLED", found.getCancelReason());
        assertNotNull(found.getClosedAt());
    }

    @Test
    void createShouldCloseOrderWhenInventoryReserveFailed() {
        OrderApplicationService service = new OrderApplicationService(new TestOrderRepository(), () -> "ORD-FAIL-1",
                new FailedInventoryCommandFacade(), new SuccessPaymentCommandFacade());

        IllegalStateException exception = org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () ->
                service.create(new CreateOrderCommand(1001L, 2001L, "CNY", "MOCK", "remark",
                        Instant.parse("2026-03-30T00:00:00Z"),
                        List.of(new CreateOrderItemCommand(101L, "demo-item", 2, BigDecimal.valueOf(10))))));

        assertEquals("stock not enough", exception.getMessage());
    }

    @Test
    void createShouldReleaseInventoryWhenPaymentCreateFailed() {
        TrackingInventoryCommandFacade inventoryFacade = new TrackingInventoryCommandFacade();
        OrderApplicationService service = new OrderApplicationService(new TestOrderRepository(), () -> "ORD-FAIL-2",
                inventoryFacade, new FailedPaymentCommandFacade());

        IllegalStateException exception = org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class, () ->
                service.create(new CreateOrderCommand(1001L, 2001L, "CNY", "MOCK", "remark",
                        Instant.parse("2026-03-30T00:00:00Z"),
                        List.of(new CreateOrderItemCommand(101L, "demo-item", 2, BigDecimal.valueOf(10))))));

        assertEquals("payment channel unavailable", exception.getMessage());
        assertEquals("PAYMENT_CREATE_FAILED", inventoryFacade.lastReleaseReason);
    }

    private static final class SequenceOrderNoGenerator implements OrderNoGenerator {

        private final AtomicLong seq = new AtomicLong(10000);

        @Override
        public String nextOrderNo() {
            return "ORD-" + seq.incrementAndGet();
        }
    }

    private static final class TestOrderRepository implements OrderRepository {

        private final Map<Long, Order> storage = new ConcurrentHashMap<>();
        private final Map<Long, List<OrderItem>> itemStorage = new ConcurrentHashMap<>();
        private final Map<Long, OrderPaymentSnapshot> paymentSnapshots = new ConcurrentHashMap<>();
        private final Map<Long, OrderInventorySnapshot> inventorySnapshots = new ConcurrentHashMap<>();
        private final Map<String, List<OrderAuditLog>> auditLogs = new ConcurrentHashMap<>();
        private final AtomicLong idGenerator = new AtomicLong(1000L);

        @Override
        public Order save(Order order) {
            if (order.getId() == null) {
                order.setId(idGenerator.getAndIncrement());
            }
            storage.put(order.getId(), order);
            return order;
        }

        @Override
        public Optional<Order> findById(Long id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public Optional<Order> findByOrderNo(Long tenantId, String orderNo) {
            return storage.values().stream()
                    .filter(order -> tenantId.equals(order.getTenantId()))
                    .filter(order -> orderNo.equals(order.getOrderNo()))
                    .findFirst();
        }

        @Override
        public void saveItems(Long tenantId, Long orderId, List<OrderItem> items) {
            itemStorage.put(orderId, items == null ? List.of() : List.copyOf(items));
        }

        @Override
        public List<OrderItem> findItemsByOrderId(Long tenantId, Long orderId) {
            return itemStorage.getOrDefault(orderId, List.of()).stream()
                    .filter(item -> tenantId.equals(item.getTenantId()))
                    .toList();
        }

        @Override
        public void savePaymentSnapshot(OrderPaymentSnapshot snapshot) {
            paymentSnapshots.put(snapshot.orderId(), snapshot);
        }

        @Override
        public Optional<OrderPaymentSnapshot> findPaymentSnapshotByOrderId(Long tenantId, Long orderId) {
            OrderPaymentSnapshot snapshot = paymentSnapshots.get(orderId);
            if (snapshot == null || !tenantId.equals(snapshot.tenantId())) {
                return Optional.empty();
            }
            return Optional.of(snapshot);
        }

        @Override
        public void saveInventorySnapshot(OrderInventorySnapshot snapshot) {
            inventorySnapshots.put(snapshot.orderId(), snapshot);
        }

        @Override
        public Optional<OrderInventorySnapshot> findInventorySnapshotByOrderId(Long tenantId, Long orderId) {
            OrderInventorySnapshot snapshot = inventorySnapshots.get(orderId);
            if (snapshot == null || !tenantId.equals(snapshot.tenantId())) {
                return Optional.empty();
            }
            return Optional.of(snapshot);
        }

        @Override
        public void saveAuditLog(OrderAuditLog auditLog) {
            String key = auditLog.tenantId() + ":" + auditLog.orderNo();
            auditLogs.computeIfAbsent(key, unused -> new java.util.ArrayList<>()).add(auditLog);
        }

        @Override
        public List<OrderAuditLog> findAuditLogs(Long tenantId, String orderNo) {
            return List.copyOf(auditLogs.getOrDefault(tenantId + ":" + orderNo, List.of()));
        }

        @Override
        public List<Order> findAll() {
            return storage.values().stream().toList();
        }
    }

    private static class SuccessInventoryCommandFacade implements InventoryCommandFacade {

        @Override
        public InventoryReservationResultDTO reserveStock(Long tenantId, String orderNo,
                                                          List<InventoryReservationItemDTO> items) {
            return new InventoryReservationResultDTO(tenantId, orderNo, "RSV-" + orderNo, "RESERVED", "RESERVED",
                    1L, null, null, null, null);
        }

        @Override
        public InventoryReservationResultDTO releaseReservedStock(Long tenantId, String orderNo, String reason) {
            return new InventoryReservationResultDTO(tenantId, orderNo, "RSV-" + orderNo, "RELEASED", "RELEASED",
                    1L, null, reason, Instant.now(), null);
        }

        @Override
        public InventoryReservationResultDTO deductReservedStock(Long tenantId, String orderNo) {
            return new InventoryReservationResultDTO(tenantId, orderNo, "RSV-" + orderNo, "DEDUCTED", "DEDUCTED",
                    1L, null, null, null, Instant.now());
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
                    1L, "stock not enough", null, null, null);
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
