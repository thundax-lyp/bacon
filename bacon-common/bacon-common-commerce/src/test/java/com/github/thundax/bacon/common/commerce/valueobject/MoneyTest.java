package com.github.thundax.bacon.common.commerce.valueobject;

import com.github.thundax.bacon.common.core.enums.CurrencyCode;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MoneyTest {

    @Test
    void shouldNormalizeScaleAndExposeStableZero() {
        assertEquals(new BigDecimal("18.80"), Money.of(new BigDecimal("18.8")).value());
        assertEquals(new BigDecimal("0.00"), Money.zero().value());
        assertEquals(CurrencyCode.RMB, Money.zero().currencyCode());
    }

    @Test
    void shouldSupportExplicitCurrencyCode() {
        Money money = Money.of(new BigDecimal("10"), CurrencyCode.USD);

        assertEquals(CurrencyCode.USD, money.currencyCode());
        assertEquals("USD 10.00", money.toString());
    }

    @Test
    void shouldRejectCrossCurrencyComparison() {
        assertThrows(IllegalArgumentException.class,
                () -> Money.of(new BigDecimal("10"), CurrencyCode.RMB)
                        .compareTo(Money.of(new BigDecimal("10"), CurrencyCode.USD)));
    }
}
