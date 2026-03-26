package com.github.thundax.bacon.order.infra.persistence.repositoryimpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderPageQuery;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderPageResult;
import com.github.thundax.bacon.order.infra.persistence.dataobject.OrderDataObject;
import com.github.thundax.bacon.order.infra.persistence.mapper.OrderAuditLogMapper;
import com.github.thundax.bacon.order.infra.persistence.mapper.OrderInventorySnapshotMapper;
import com.github.thundax.bacon.order.infra.persistence.mapper.OrderItemMapper;
import com.github.thundax.bacon.order.infra.persistence.mapper.OrderMapper;
import com.github.thundax.bacon.order.infra.persistence.mapper.OrderPaymentSnapshotMapper;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OrderRepositorySupportPagingTest {

    @Test
    void shouldPageBySqlLimitInsteadOfApplicationMemoryFiltering() {
        AtomicReference<LambdaQueryWrapper<OrderDataObject>> pagedWrapperRef = new AtomicReference<>();
        OrderMapper orderMapper = createOrderMapper(pagedWrapperRef);
        OrderPaymentSnapshotMapper paymentMapper = createSnapshotMapper(OrderPaymentSnapshotMapper.class);
        OrderInventorySnapshotMapper inventoryMapper = createSnapshotMapper(OrderInventorySnapshotMapper.class);

        OrderRepositorySupport support = new OrderRepositorySupport(orderMapper,
                createNoopMapper(OrderItemMapper.class), paymentMapper, inventoryMapper,
                createNoopMapper(OrderAuditLogMapper.class));

        OrderPageResult result = support.pageOrders(new OrderPageQuery(
                1001L, null, null, null, null, null,
                null, null, 1, 2));

        assertEquals(3L, result.total());
        assertEquals(2, result.records().size());
        assertEquals("ORD-1002", result.records().get(0).getOrderNo());
        assertEquals("ORD-1001", result.records().get(1).getOrderNo());

        LambdaQueryWrapper<OrderDataObject> pagedWrapper = pagedWrapperRef.get();
        assertNotNull(pagedWrapper);
    }

    @SuppressWarnings("unchecked")
    private OrderMapper createOrderMapper(AtomicReference<LambdaQueryWrapper<OrderDataObject>> pagedWrapperRef) {
        return (OrderMapper) Proxy.newProxyInstance(OrderMapper.class.getClassLoader(),
                new Class[]{OrderMapper.class}, (proxy, method, args) -> {
                    if ("selectCount".equals(method.getName())) {
                        return 3L;
                    }
                    if ("selectList".equals(method.getName())) {
                        LambdaQueryWrapper<OrderDataObject> wrapper = (LambdaQueryWrapper<OrderDataObject>) args[0];
                        pagedWrapperRef.set(wrapper);
                        return List.of(
                                buildOrder(2L, "ORD-1002", Instant.parse("2026-03-26T11:00:00Z")),
                                buildOrder(1L, "ORD-1001", Instant.parse("2026-03-26T10:00:00Z"))
                        );
                    }
                    return null;
                });
    }

    @SuppressWarnings("unchecked")
    private <T> T createSnapshotMapper(Class<T> mapperType) {
        return (T) Proxy.newProxyInstance(mapperType.getClassLoader(), new Class[]{mapperType},
                (proxy, method, args) -> {
                    if ("selectList".equals(method.getName())) {
                        return List.of();
                    }
                    return null;
                });
    }

    @SuppressWarnings("unchecked")
    private <T> T createNoopMapper(Class<T> mapperType) {
        return (T) Proxy.newProxyInstance(mapperType.getClassLoader(), new Class[]{mapperType},
                (proxy, method, args) -> null);
    }

    private OrderDataObject buildOrder(Long id, String orderNo, Instant createdAt) {
        return new OrderDataObject(id, 1001L, orderNo, 2001L,
                "CREATED", "UNPAID", "UNRESERVED", "CNY",
                BigDecimal.TEN, BigDecimal.TEN, "remark", null,
                null, createdAt, createdAt, createdAt.plusSeconds(1800), null, null);
    }
}
