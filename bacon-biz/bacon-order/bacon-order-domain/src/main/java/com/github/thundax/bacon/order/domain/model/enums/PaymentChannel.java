package com.github.thundax.bacon.order.domain.model.enums;

import java.util.Arrays;

/**
 * 支付渠道。
 */
public enum PaymentChannel {
    ALIPAY,
    WECHAT,
    APPLE,
    MOCK;

    public String value() {
        return name();
    }

    public static PaymentChannel fromValue(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported payment channel: " + value));
    }
}
