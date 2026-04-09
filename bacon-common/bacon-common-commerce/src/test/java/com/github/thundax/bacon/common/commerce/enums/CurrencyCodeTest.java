package com.github.thundax.bacon.common.commerce.enums;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CurrencyCodeTest {

    @Test
    void shouldResolveStableCurrencyCodes() {
        assertEquals(CurrencyCode.RMB, CurrencyCode.fromValue("RMB"));
        assertEquals(CurrencyCode.RMB, CurrencyCode.fromValue("CNY"));
        assertEquals(CurrencyCode.USD, CurrencyCode.fromValue("USD"));
        assertEquals(CurrencyCode.JPY, CurrencyCode.fromValue("JPY"));
    }

    @Test
    void shouldRejectUnsupportedCurrencyCode() {
        assertThrows(IllegalArgumentException.class, () -> CurrencyCode.fromValue("EUR"));
    }
}
