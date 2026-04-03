package com.github.thundax.bacon.common.core.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CurrencyCodeTest {

    @Test
    void shouldResolveStableCurrencyCodes() {
        assertEquals(CurrencyCode.CNY, CurrencyCode.fromValue("CNY"));
        assertEquals(CurrencyCode.USD, CurrencyCode.fromValue("USD"));
        assertEquals(CurrencyCode.JPY, CurrencyCode.fromValue("JPY"));
    }

    @Test
    void shouldRejectUnsupportedCurrencyCode() {
        assertThrows(IllegalArgumentException.class, () -> CurrencyCode.fromValue("EUR"));
    }
}
