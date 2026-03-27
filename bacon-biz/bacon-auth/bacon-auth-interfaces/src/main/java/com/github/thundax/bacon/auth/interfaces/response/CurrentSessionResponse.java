package com.github.thundax.bacon.auth.interfaces.response;

import com.github.thundax.bacon.auth.api.dto.CurrentSessionDTO;
import java.time.Instant;

public record CurrentSessionResponse(String sessionId, Long tenantId, Long userId, String identityType,
                                     String loginType, String sessionStatus, Instant issuedAt,
                                     Instant lastAccessTime, Instant expireAt) {

    public static CurrentSessionResponse from(CurrentSessionDTO dto) {
        return new CurrentSessionResponse(dto.getSessionId(), dto.getTenantId(), dto.getUserId(),
                dto.getIdentityType(), dto.getLoginType(), dto.getSessionStatus(), dto.getIssuedAt(),
                dto.getLastAccessTime(), dto.getExpireAt());
    }
}
