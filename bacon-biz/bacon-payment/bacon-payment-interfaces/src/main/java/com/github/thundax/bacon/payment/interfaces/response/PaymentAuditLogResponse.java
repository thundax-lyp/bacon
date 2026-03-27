package com.github.thundax.bacon.payment.interfaces.response;

import java.time.Instant;

public record PaymentAuditLogResponse(Long tenantId, String paymentNo, String actionType, String beforeStatus,
                                      String afterStatus, String operatorType, Long operatorId, Instant occurredAt) {
}
