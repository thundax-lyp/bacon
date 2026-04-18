package com.github.thundax.bacon.common.commerce.valueobject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.thundax.bacon.common.commerce.enums.CurrencyCode;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

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
    void shouldMultiplyAndKeepCurrency() {
        Money money = Money.of(new BigDecimal("10.50"), CurrencyCode.USD);

        Money result = money.multiply(new BigDecimal("3"));

        assertEquals(CurrencyCode.USD, result.currencyCode());
        assertEquals(new BigDecimal("31.50"), result.value());
    }

    @Test
    void shouldRejectCrossCurrencyComparison() {
        assertThrows(IllegalArgumentException.class, () -> Money.of(new BigDecimal("10"), CurrencyCode.RMB)
                .compareTo(Money.of(new BigDecimal("10"), CurrencyCode.USD)));
    }
}
