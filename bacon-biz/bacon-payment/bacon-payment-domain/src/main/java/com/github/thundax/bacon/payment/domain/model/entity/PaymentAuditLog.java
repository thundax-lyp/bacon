package com.github.thundax.bacon.payment.domain.model.entity;

import lombok.Getter;

import java.time.Instant;

@Getter
public class PaymentAuditLog {

    public static final String ACTION_CREATE = "CREATE";
    public static final String ACTION_CALLBACK_PAID = "CALLBACK_PAID";
    public static final String ACTION_CALLBACK_FAILED = "CALLBACK_FAILED";
    public static final String ACTION_CLOSE = "CLOSE";
    public static final String OPERATOR_SYSTEM = "SYSTEM";
    public static final String OPERATOR_CHANNEL = "CHANNEL";

    private final Long id;
    private final Long tenantId;
    private final String paymentNo;
    private final String actionType;
    private final String beforeStatus;
    private final String afterStatus;
    private final String operatorType;
    private final Long operatorId;
    private final Instant occurredAt;

    public PaymentAuditLog(Long id, Long tenantId, String paymentNo, String actionType, String beforeStatus,
                           String afterStatus, String operatorType, Long operatorId, Instant occurredAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.paymentNo = paymentNo;
        this.actionType = actionType;
        this.beforeStatus = beforeStatus;
        this.afterStatus = afterStatus;
        this.operatorType = operatorType;
        this.operatorId = operatorId;
        this.occurredAt = occurredAt;
    }
}
