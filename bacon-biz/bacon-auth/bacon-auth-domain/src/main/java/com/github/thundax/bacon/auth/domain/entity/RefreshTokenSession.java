package com.github.thundax.bacon.auth.domain.entity;

import java.time.Instant;
import lombok.Getter;

@Getter
public class RefreshTokenSession {

    private final String sessionId;
    private final String refreshTokenHash;
    private final Instant issuedAt;
    private final Instant expireAt;
    private String tokenStatus;
    private Instant usedAt;

    public RefreshTokenSession(String sessionId, String refreshTokenHash, Instant issuedAt, Instant expireAt) {
        this.sessionId = sessionId;
        this.refreshTokenHash = refreshTokenHash;
        this.issuedAt = issuedAt;
        this.expireAt = expireAt;
        this.tokenStatus = "ACTIVE";
    }

    public void markUsed(Instant useTime) {
        this.tokenStatus = "USED";
        this.usedAt = useTime;
    }

    public void invalidate() {
        this.tokenStatus = "INVALIDATED";
    }

    public void expire() {
        this.tokenStatus = "EXPIRED";
    }
}
