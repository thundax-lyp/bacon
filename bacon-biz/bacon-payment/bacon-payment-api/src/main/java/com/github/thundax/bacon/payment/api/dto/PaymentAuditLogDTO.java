package com.github.thundax.bacon.payment.api.dto;

import java.time.Instant;

public record PaymentAuditLogDTO(Long tenantId, String paymentNo, String actionType, String beforeStatus,
                                 String afterStatus, String operatorType, Long operatorId, Instant occurredAt) {
}
