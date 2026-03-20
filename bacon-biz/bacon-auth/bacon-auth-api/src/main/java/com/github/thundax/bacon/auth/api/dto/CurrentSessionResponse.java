package com.github.thundax.bacon.auth.api.dto;

import java.time.Instant;

public record CurrentSessionResponse(
        String sessionId,
        Long tenantId,
        Long userId,
        String identityType,
        String loginType,
        String sessionStatus,
        Instant issuedAt,
        Instant lastAccessTime,
        Instant expireAt) {
}
