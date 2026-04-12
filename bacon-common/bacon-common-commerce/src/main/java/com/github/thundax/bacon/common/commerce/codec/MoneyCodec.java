package com.github.thundax.bacon.common.commerce.codec;

import com.github.thundax.bacon.common.commerce.enums.CurrencyCode;
import com.github.thundax.bacon.common.commerce.valueobject.Money;
import java.math.BigDecimal;

public final class MoneyCodec {

    private MoneyCodec() {}

    public static Money toDomain(BigDecimal value) {
        return value == null ? null : Money.of(value);
    }

    public static Money toDomain(BigDecimal value, CurrencyCode currencyCode) {
        return value == null || currencyCode == null ? null : Money.of(value, currencyCode);
    }

    public static BigDecimal toValue(Money money) {
        return money == null ? null : money.value();
    }

    public static CurrencyCode toCurrencyCode(Money money) {
        return money == null ? null : money.currencyCode();
    }
}
