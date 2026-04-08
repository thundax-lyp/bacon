package com.github.thundax.bacon.auth.domain.model.entity;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 刷新令牌会话领域实体。
 */
@Getter
@AllArgsConstructor
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
        this(sessionId, refreshTokenHash, issuedAt, expireAt, "ACTIVE", null);
    }

    public void markUsed(Instant useTime) {
        // 会话维度同样记录一次性消费时间，用来和 refresh token 主表交叉校验是否发生重放。
        this.tokenStatus = "USED";
        this.usedAt = useTime;
    }

    public void invalidate() {
        // 失效通常来自安全策略或登出清理，和自然过期区分处理。
        this.tokenStatus = "INVALIDATED";
    }

    public void expire() {
        // 过期是时间驱动的终态，不记录 usedAt，避免和真实消费混淆。
        this.tokenStatus = "EXPIRED";
    }
}
