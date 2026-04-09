package com.github.thundax.bacon.common.commerce.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
