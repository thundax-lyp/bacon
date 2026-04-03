package com.github.thundax.bacon.common.core.valueobject;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MoneyTest {

    @Test
    void shouldNormalizeScaleAndExposeStableZero() {
        assertEquals(new BigDecimal("18.80"), Money.of(new BigDecimal("18.8")).value());
        assertEquals(new BigDecimal("0.00"), Money.zero().value());
    }
}
