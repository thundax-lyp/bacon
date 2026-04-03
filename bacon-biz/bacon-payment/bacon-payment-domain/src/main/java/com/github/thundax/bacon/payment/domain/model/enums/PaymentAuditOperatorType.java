package com.github.thundax.bacon.payment.domain.model.enums;

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
        return value == null ? null : PaymentAuditOperatorType.valueOf(value);
    }
}
