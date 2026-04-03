package com.github.thundax.bacon.order.domain.model.enums;

/**
 * 订单审计动作类型。
 */
public enum OrderAuditActionType {

    ORDER_CREATE("ORDER_CREATE"),
    ORDER_CANCEL("ORDER_CANCEL"),
    ORDER_MARK_PAID("ORDER_MARK_PAID"),
    ORDER_MARK_PAYMENT_FAILED("ORDER_MARK_PAYMENT_FAILED"),
    ORDER_CLOSE_EXPIRED("ORDER_CLOSE_EXPIRED"),
    OUTBOX_RESERVE_FAILED("OUTBOX_RESERVE_FAILED"),
    OUTBOX_RESERVE_OK("OUTBOX_RESERVE_OK"),
    OUTBOX_CREATE_PAYMENT_FAILED("OUTBOX_CREATE_PAYMENT_FAILED"),
    OUTBOX_CREATE_PAYMENT_OK("OUTBOX_CREATE_PAYMENT_OK"),
    OUTBOX_RELEASE("OUTBOX_RELEASE");

    private final String value;

    OrderAuditActionType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static OrderAuditActionType fromValue(String value) {
        for (OrderAuditActionType actionType : values()) {
            if (actionType.value.equals(value)) {
                return actionType;
            }
        }
        throw new IllegalArgumentException("Unsupported order audit action type: " + value);
    }
}
