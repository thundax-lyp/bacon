package com.github.thundax.bacon.order.domain.model.enums;

import java.util.Arrays;

/**
 * 订单审计动作类型。
 */
public enum OrderAuditActionType {
    ORDER_CREATE,
    ORDER_CANCEL,
    ORDER_MARK_PAID,
    ORDER_MARK_PAYMENT_FAILED,
    ORDER_CLOSE_EXPIRED,
    OUTBOX_RESERVE_FAILED,
    OUTBOX_RESERVE_OK,
    OUTBOX_CREATE_PAYMENT_FAILED,
    OUTBOX_CREATE_PAYMENT_OK,
    OUTBOX_RELEASE;

    public String value() {
        return name();
    }

    public static OrderAuditActionType fromValue(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported order audit action type: " + value));
    }
}
