package com.github.thundax.bacon.order.domain.model.entity;

import java.time.Instant;

public record OrderAuditLog(
        Long id,
        Long tenantId,
        String orderNo,
        String actionType,
        String beforeStatus,
        String afterStatus,
        String operatorType,
        Long operatorId,
        Instant occurredAt
) {
}
