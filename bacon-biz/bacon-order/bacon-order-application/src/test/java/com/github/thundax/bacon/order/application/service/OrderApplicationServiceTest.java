package com.github.thundax.bacon.order.application.service;

import com.github.thundax.bacon.order.api.dto.OrderSummaryDTO;
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
