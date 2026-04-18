package com.github.thundax.bacon.order.domain.model.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.thundax.bacon.common.commerce.enums.CurrencyCode;
import com.github.thundax.bacon.common.commerce.identifier.SkuId;
import com.github.thundax.bacon.common.commerce.valueobject.Money;
import com.github.thundax.bacon.order.domain.exception.OrderDomainException;
import com.github.thundax.bacon.order.domain.model.valueobject.OrderId;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class OrderItemTest {

    @Test
    void createShouldCalculateLineAmount() {
        OrderItem item = OrderItem.create(
                1L,
                OrderId.of(10L),
                SkuId.of(1001L),
                "sku",
                null,
                3,
                Money.of(new BigDecimal("9.90"), CurrencyCode.RMB));

        assertEquals(Money.of(new BigDecimal("29.70"), CurrencyCode.RMB), item.getLineAmount());
    }

    @Test
    void reconstructShouldRejectMismatchedLineAmount() {
        assertThrows(
                OrderDomainException.class,
                () -> OrderItem.reconstruct(
                        1L,
                        OrderId.of(10L),
                        SkuId.of(1001L),
                        "sku",
                        null,
                        2,
                        Money.of(new BigDecimal("9.90"), CurrencyCode.RMB),
                        Money.of(new BigDecimal("10.00"), CurrencyCode.RMB)));
    }

    @Test
    void createShouldRejectInvalidQuantity() {
        assertThrows(
                OrderDomainException.class,
                () -> OrderItem.create(
                        1L,
                        OrderId.of(10L),
                        SkuId.of(1001L),
                        "sku",
                        null,
                        0,
                        Money.of(new BigDecimal("9.90"), CurrencyCode.RMB)));
    }
}
