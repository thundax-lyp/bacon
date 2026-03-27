package com.github.thundax.bacon.auth.domain.model.entity;

import lombok.Getter;

import java.time.Instant;

/**
 * 刷新令牌会话领域实体。
 */
@Getter
public class RefreshTokenSession {

    /** 会话标识。 */
    private final String sessionId;
    /** 刷新令牌哈希。 */
    private final String refreshTokenHash;
    /** 签发时间。 */
    private final Instant issuedAt;
    /** 过期时间。 */
    private final Instant expireAt;
    /** 令牌状态。 */
    private String tokenStatus;
    /** 使用时间。 */
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
