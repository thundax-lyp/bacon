package com.github.thundax.bacon.common.commerce.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.github.thundax.bacon.common.commerce.enums.CurrencyCode;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MoneyCodecTest {

    @Test
    void shouldReturnNullForNullInput() {
        assertNull(MoneyCodec.toDomain((BigDecimal) null));
        assertNull(MoneyCodec.toDomain(null, CurrencyCode.RMB));
        assertNull(MoneyCodec.toDomain(BigDecimal.ONE, null));
        assertNull(MoneyCodec.toValue(null));
        assertNull(MoneyCodec.toCurrencyCode(null));
    }

    @Test
    void shouldConvertMoneyWithDefaultCurrency() {
        assertEquals(new BigDecimal("18.80"), MoneyCodec.toDomain(new BigDecimal("18.8")).value());
        assertEquals(CurrencyCode.RMB, MoneyCodec.toDomain(new BigDecimal("18.8")).currencyCode());
    }

    @Test
    void shouldConvertMoneyWithExplicitCurrency() {
        assertEquals(new BigDecimal("66.00"), MoneyCodec.toValue(MoneyCodec.toDomain(new BigDecimal("66"), CurrencyCode.USD)));
        assertEquals(CurrencyCode.USD, MoneyCodec.toCurrencyCode(MoneyCodec.toDomain(new BigDecimal("66"), CurrencyCode.USD)));
    }
}
