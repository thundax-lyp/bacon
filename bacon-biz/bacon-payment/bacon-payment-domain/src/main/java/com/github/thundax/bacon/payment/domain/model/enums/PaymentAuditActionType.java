package com.github.thundax.bacon.payment.domain.model.enums;

import java.util.Arrays;

/**
 * 支付审计动作类型。
 */
public enum PaymentAuditActionType {
    CREATE,
    CALLBACK_PAID,
    CALLBACK_FAILED,
    CLOSE;

    public String value() {
        return name();
    }

    public static PaymentAuditActionType fromValue(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown payment audit action type: " + value));
    }
}
