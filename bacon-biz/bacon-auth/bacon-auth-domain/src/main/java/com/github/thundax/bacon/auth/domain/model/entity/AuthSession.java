package com.github.thundax.bacon.auth.domain.model.entity;

import lombok.Getter;

import java.time.Instant;

/**
 * 认证会话领域实体。
 */
@Getter
public class AuthSession {

    /** 会话主键。 */
    private final Long id;
    /** 会话标识。 */
    private final String sessionId;
    /** 所属租户主键。 */
    private final Long tenantId;
    /** 用户主键。 */
    private final Long userId;
    /** 身份标识值。 */
    private final String identityId;
    /** 身份标识类型。 */
    private final String identityType;
    /** 登录方式类型。 */
    private final String loginType;
    /** 签发时间。 */
    private final Instant issuedAt;
    /** 过期时间。 */
    private final Instant expireAt;
    /** 会话状态。 */
    private String sessionStatus;
    /** 最后访问时间。 */
    private Instant lastAccessTime;
    /** 登出时间。 */
    private Instant logoutAt;
    /** 失效原因。 */
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
