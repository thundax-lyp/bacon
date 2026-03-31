package com.github.thundax.bacon.upms.domain.model.entity;

import java.time.LocalDateTime;
import lombok.Getter;

/**
 * 用户认证凭据领域实体。
 */
@Getter
public class UserCredential {

    private Long id;
    private Long tenantId;
    private Long userId;
    private Long identityId;
    private String credentialType;
    private String factorLevel;
    private String credentialValue;
    private String status;
    private boolean needChangePassword;
    private int failedCount;
    private int failedLimit;
    private String lockReason;
    private LocalDateTime lockedUntil;
    private LocalDateTime expiresAt;
    private LocalDateTime lastVerifiedAt;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;

    public UserCredential(Long id, Long tenantId, Long userId, Long identityId, String credentialType,
                          String factorLevel, String credentialValue, String status, boolean needChangePassword,
                          int failedCount, int failedLimit, String lockReason, LocalDateTime lockedUntil,
                          LocalDateTime expiresAt, LocalDateTime lastVerifiedAt) {
        this(id, tenantId, userId, identityId, credentialType, factorLevel, credentialValue, status,
                needChangePassword, failedCount, failedLimit, lockReason, lockedUntil, expiresAt,
                lastVerifiedAt, null, null, null, null);
    }

    public UserCredential(Long id, Long tenantId, Long userId, Long identityId, String credentialType,
                          String factorLevel, String credentialValue, String status, boolean needChangePassword,
                          int failedCount, int failedLimit, String lockReason, LocalDateTime lockedUntil,
                          LocalDateTime expiresAt, LocalDateTime lastVerifiedAt, String createdBy,
                          LocalDateTime createdAt, String updatedBy, LocalDateTime updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.userId = userId;
        this.identityId = identityId;
        this.credentialType = credentialType;
        this.factorLevel = factorLevel;
        this.credentialValue = credentialValue;
        this.status = status;
        this.needChangePassword = needChangePassword;
        this.failedCount = failedCount;
        this.failedLimit = failedLimit;
        this.lockReason = lockReason;
        this.lockedUntil = lockedUntil;
        this.expiresAt = expiresAt;
        this.lastVerifiedAt = lastVerifiedAt;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }
}
