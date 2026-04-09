package com.github.thundax.bacon.common.commerce.enums;

/**
 * 币种编码。
 */
public enum CurrencyCode {

    RMB("RMB"),
    USD("USD"),
    JPY("JPY");

    private final String value;

    CurrencyCode(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static CurrencyCode fromValue(String value) {
        if ("CNY".equals(value)) {
            return RMB;
        }
        for (CurrencyCode currencyCode : values()) {
            if (currencyCode.value.equals(value)) {
                return currencyCode;
            }
        }
        throw new IllegalArgumentException("Unsupported currency code: " + value);
    }
}
