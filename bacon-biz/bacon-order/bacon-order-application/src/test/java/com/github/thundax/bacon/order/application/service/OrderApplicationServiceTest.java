package com.github.thundax.bacon.order.application.service;

import com.github.thundax.bacon.order.api.dto.OrderSummaryDTO;
import com.github.thundax.bacon.order.api.dto.OrderPageQueryDTO;
import com.github.thundax.bacon.order.api.dto.OrderPageResultDTO;
import com.github.thundax.bacon.order.application.command.CreateOrderCommand;
import com.github.thundax.bacon.order.application.command.CreateOrderItemCommand;
import com.github.thundax.bacon.order.domain.model.entity.Order;
import com.github.thundax.bacon.order.domain.repository.OrderRepository;
import com.github.thundax.bacon.order.domain.service.OrderNoGenerator;
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
        OrderApplicationService service = new OrderApplicationService(new TestOrderRepository(),
                () -> "ORD-10001");

        OrderSummaryDTO result = service.create(new CreateOrderCommand(1001L, 2001L, "CNY", "MOCK", "remark",
                Instant.parse("2026-03-30T00:00:00Z"),
                List.of(new CreateOrderItemCommand(101L, "demo-item", 2, BigDecimal.valueOf(10)))));

        assertEquals("ORD-10001", result.getOrderNo());
        assertEquals(1001L, result.getTenantId());
        assertEquals(BigDecimal.valueOf(20), result.getTotalAmount());
        assertEquals("CREATED", result.getOrderStatus());
    }

    @Test
    void pageOrdersShouldRespectFilterAndPaging() {
        TestOrderRepository repository = new TestOrderRepository();
        OrderApplicationService service = new OrderApplicationService(repository,
                new SequenceOrderNoGenerator());
        service.create(new CreateOrderCommand(1001L, 2001L, "CNY", "MOCK", "r1",
                Instant.parse("2026-03-30T00:00:00Z"),
                List.of(new CreateOrderItemCommand(101L, "item-1", 1, BigDecimal.valueOf(10)))));
        OrderSummaryDTO paid = service.create(new CreateOrderCommand(1001L, 2001L, "CNY", "MOCK", "r2",
                Instant.parse("2026-03-30T00:00:00Z"),
                List.of(new CreateOrderItemCommand(102L, "item-2", 1, BigDecimal.valueOf(20)))));
        service.create(new CreateOrderCommand(1002L, 2002L, "CNY", "MOCK", "r3",
                Instant.parse("2026-03-30T00:00:00Z"),
                List.of(new CreateOrderItemCommand(103L, "item-3", 1, BigDecimal.valueOf(30)))));
        service.markPaid(1001L, paid.getOrderNo(), "PAY-1", Instant.parse("2026-03-26T10:00:00Z"));

        OrderPageResultDTO page = service.pageOrders(new OrderPageQueryDTO(1001L, 2001L, null, null, "UNPAID",
                null, null, null, 1, 10));

        assertEquals(1, page.getTotal());
        assertEquals(1, page.getRecords().size());
        assertEquals("UNPAID", page.getRecords().get(0).getPayStatus());
    }

    @Test
    void cancelShouldPersistGivenReason() {
        TestOrderRepository repository = new TestOrderRepository();
        OrderApplicationService service = new OrderApplicationService(repository,
                () -> "ORD-CANCEL-1");
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

    private static final class SequenceOrderNoGenerator implements OrderNoGenerator {

        private final AtomicLong seq = new AtomicLong(10000);

        @Override
        public String nextOrderNo() {
            return "ORD-" + seq.incrementAndGet();
        }
    }

    private static final class TestOrderRepository implements OrderRepository {

        private final Map<Long, Order> storage = new ConcurrentHashMap<>();
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
        public List<Order> findAll() {
            return storage.values().stream().toList();
        }
    }
}
