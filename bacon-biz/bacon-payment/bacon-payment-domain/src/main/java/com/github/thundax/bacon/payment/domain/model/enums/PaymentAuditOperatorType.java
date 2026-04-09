package com.github.thundax.bacon.payment.domain.model.enums;

import java.util.Arrays;

/**
 * 支付审计操作人类型。
 */
public enum PaymentAuditOperatorType {
    SYSTEM,
    CHANNEL;

    public String value() {
        return name();
    }

    public static PaymentAuditOperatorType fromValue(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown payment audit operator type: " + value));
    }
}
