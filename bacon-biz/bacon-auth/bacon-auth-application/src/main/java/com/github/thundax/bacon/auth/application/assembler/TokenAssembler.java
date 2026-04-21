package com.github.thundax.bacon.auth.application.assembler;

import com.github.thundax.bacon.auth.application.dto.CurrentSessionDTO;
import com.github.thundax.bacon.auth.application.dto.UserTokenRefreshDTO;
import com.github.thundax.bacon.auth.api.dto.SessionValidationDTO;
import java.time.Instant;

public final class TokenAssembler {

    private TokenAssembler() {}

    public static UserTokenRefreshDTO toUserTokenRefreshDto(
            String accessToken, String refreshToken, String tokenType, long expiresIn, String sessionId) {
        return new UserTokenRefreshDTO(accessToken, refreshToken, tokenType, expiresIn, sessionId);
    }

    public static SessionValidationDTO toSessionValidationDto(
            boolean valid,
            Long tenantId,
            Long userId,
            String sessionId,
            Long identityId,
            String identityType,
            Instant expiresAt) {
        return new SessionValidationDTO(valid, tenantId, userId, sessionId, identityId, identityType, expiresAt);
    }

    public static CurrentSessionDTO toCurrentSessionDto(
            String sessionId,
            Long tenantId,
            Long userId,
            String identityType,
            String loginType,
            String status,
            Instant issuedAt,
            Instant lastAccessTime,
            Instant expireAt) {
        return new CurrentSessionDTO(
                sessionId, tenantId, userId, identityType, loginType, status, issuedAt, lastAccessTime, expireAt);
    }
}
