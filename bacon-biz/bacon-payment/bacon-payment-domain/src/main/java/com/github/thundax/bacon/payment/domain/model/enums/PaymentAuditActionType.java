package com.github.thundax.bacon.payment.domain.model.enums;

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
        return value == null ? null : PaymentAuditActionType.valueOf(value);
    }
}
