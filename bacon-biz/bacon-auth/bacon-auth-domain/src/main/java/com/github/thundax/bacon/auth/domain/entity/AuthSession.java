package com.github.thundax.bacon.auth.domain.entity;

import lombok.Getter;

import java.time.Instant;

@Getter
public class AuthSession {

    private final Long id;
    private final String sessionId;
    private final Long tenantId;
    private final Long userId;
    private final String identityId;
    private final String identityType;
    private final String loginType;
    private final Instant issuedAt;
    private final Instant expireAt;
    private String sessionStatus;
    private Instant lastAccessTime;
    private Instant logoutAt;
    private String invalidateReason;

    public AuthSession(Long id, String sessionId, Long tenantId, Long userId, String identityId, String identityType,
                       String loginType, Instant issuedAt, Instant expireAt) {
        this.id = id;
        this.sessionId = sessionId;
        this.tenantId = tenantId;
        this.userId = userId;
        this.identityId = identityId;
        this.identityType = identityType;
        this.loginType = loginType;
        this.issuedAt = issuedAt;
        this.expireAt = expireAt;
        this.sessionStatus = "ACTIVE";
        this.lastAccessTime = issuedAt;
    }

    public void touch(Instant accessTime) {
        this.lastAccessTime = accessTime;
    }

    public void logout(Instant logoutTime) {
        this.sessionStatus = "LOGGED_OUT";
        this.logoutAt = logoutTime;
    }

    public void invalidate(String reason) {
        this.sessionStatus = "INVALIDATED";
        this.invalidateReason = reason;
    }

    public void expire() {
        this.sessionStatus = "EXPIRED";
    }
}
