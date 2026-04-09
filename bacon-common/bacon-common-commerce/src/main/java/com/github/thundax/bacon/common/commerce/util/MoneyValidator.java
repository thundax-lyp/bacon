package com.github.thundax.bacon.common.commerce.util;

import com.github.thundax.bacon.common.core.enums.CurrencyCode;
import com.github.thundax.bacon.common.commerce.valueobject.Money;

public final class MoneyValidator {

    private MoneyValidator() {
    }

    public static void ensureSameCurrency(Money base, Money... others) {
        if (base == null || others == null) {
            return;
        }
        for (Money other : others) {
            if (other == null) {
                continue;
            }
            if (base.currencyCode() != other.currencyCode()) {
                throw new IllegalArgumentException("money currency code mismatch");
            }
        }
    }

    public static void ensureSameCurrency(CurrencyCode currencyCode, Money... monies) {
        if (currencyCode == null || monies == null) {
            return;
        }
        for (Money money : monies) {
            if (money == null) {
                continue;
            }
            if (currencyCode != money.currencyCode()) {
                throw new IllegalArgumentException("currency code mismatch");
            }
        }
    }
}
