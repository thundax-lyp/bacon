package com.github.thundax.bacon.auth.api.dto;

import java.time.Instant;

public record SessionValidationResponse(
        boolean valid,
        Long tenantId,
        Long userId,
        String sessionId,
        String identityId,
        String identityType,
        Instant expireAt) {
}
