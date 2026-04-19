package com.github.thundax.bacon.auth.api.response;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentSessionFacadeResponse {

    private String sessionId;
    private Long tenantId;
    private Long userId;
    private String identityType;
    private String loginType;
    private String sessionStatus;
    private Instant issuedAt;
    private Instant lastAccessTime;
    private Instant expireAt;

    public static CurrentSessionFacadeResponse from(
            String sessionId,
            Long tenantId,
            Long userId,
            String identityType,
            String loginType,
            String sessionStatus,
            Instant issuedAt,
            Instant lastAccessTime,
            Instant expireAt) {
        return new CurrentSessionFacadeResponse(
                sessionId, tenantId, userId, identityType, loginType, sessionStatus, issuedAt, lastAccessTime, expireAt);
    }
}
