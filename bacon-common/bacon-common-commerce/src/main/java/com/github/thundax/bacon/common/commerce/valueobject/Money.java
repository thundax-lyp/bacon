package com.github.thundax.bacon.common.commerce.valueobject;

import com.github.thundax.bacon.common.core.enums.CurrencyCode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * 金额值对象。
 */
public final class Money implements Comparable<Money> {

    private static final int SCALE = 2;
    private static final Money ZERO = new Money(BigDecimal.ZERO, CurrencyCode.RMB);

    private final BigDecimal value;
    private final CurrencyCode currencyCode;

    private Money(BigDecimal value, CurrencyCode currencyCode) {
        this.value = value.setScale(SCALE, RoundingMode.HALF_UP);
        this.currencyCode = Objects.requireNonNull(currencyCode, "money currencyCode must not be null");
    }

    public static Money of(BigDecimal value) {
        return of(value, CurrencyCode.RMB);
    }

    public static Money of(BigDecimal value, CurrencyCode currencyCode) {
        return new Money(Objects.requireNonNull(value, "money value must not be null"), currencyCode);
    }

    public static Money zero() {
        return ZERO;
    }

    public BigDecimal value() {
        return value;
    }

    public CurrencyCode currencyCode() {
        return currencyCode;
    }

    @Override
    public int compareTo(Money other) {
        ensureSameCurrency(other);
        return value.compareTo(other.value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Money other)) {
            return false;
        }
        return currencyCode == other.currencyCode && value.compareTo(other.value) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(currencyCode, value.stripTrailingZeros());
    }

    @Override
    public String toString() {
        return currencyCode.value() + " " + value.toPlainString();
    }

    private void ensureSameCurrency(Money other) {
        if (currencyCode != other.currencyCode) {
            throw new IllegalArgumentException("Cannot compare money with different currency codes");
        }
    }
}
