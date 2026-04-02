package com.github.thundax.bacon.upms.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserCredentialId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.common.id.domain.UserIdentityId;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialFactorLevel;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialStatus;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialType;
import java.time.Instant;
import lombok.Getter;

/**
 * 用户认证凭据领域实体。
 */
@Getter
public class UserCredential {

    /** 凭据主键。 */
    private UserCredentialId id;
    /** 所属租户主键。 */
    private TenantId tenantId;
    /** 关联用户主键。 */
    private UserId userId;
    /** 关联身份标识主键。 */
    private UserIdentityId identityId;
    /** 凭据类型。 */
    private UserCredentialType credentialType;
    /** 因子等级。 */
    private UserCredentialFactorLevel factorLevel;
    /** 凭据值。 */
    private String credentialValue;
    /** 凭据状态。 */
    private UserCredentialStatus status;
    /** 是否需要改密。 */
    private boolean needChangePassword;
    /** 连续失败次数。 */
    private int failedCount;
    /** 最大允许失败次数。 */
    private int failedLimit;
    /** 锁定原因。 */
    private String lockReason;
    /** 锁定截止时间。 */
    private Instant lockedUntil;
    /** 过期时间。 */
    private Instant expiresAt;
    /** 最近验证时间。 */
    private Instant lastVerifiedAt;
    /** 创建人。 */
    private String createdBy;
    /** 创建时间。 */
    private Instant createdAt;
    /** 最后更新人。 */
    private String updatedBy;
    /** 最后更新时间。 */
    private Instant updatedAt;

    public UserCredential(UserCredentialId id, TenantId tenantId, UserId userId, UserIdentityId identityId, UserCredentialType credentialType,
                          UserCredentialFactorLevel factorLevel, String credentialValue, UserCredentialStatus status,
                          boolean needChangePassword,
                          int failedCount, int failedLimit, String lockReason, Instant lockedUntil,
                          Instant expiresAt, Instant lastVerifiedAt) {
        this(id, tenantId, userId, identityId, credentialType, factorLevel, credentialValue, status,
                needChangePassword, failedCount, failedLimit, lockReason, lockedUntil, expiresAt,
                lastVerifiedAt, null, null, null, null);
    }

    public UserCredential(UserCredentialId id, TenantId tenantId, UserId userId, UserIdentityId identityId, UserCredentialType credentialType,
                          UserCredentialFactorLevel factorLevel, String credentialValue, UserCredentialStatus status,
                          boolean needChangePassword,
                          int failedCount, int failedLimit, String lockReason, Instant lockedUntil,
                          Instant expiresAt, Instant lastVerifiedAt, String createdBy,
                          Instant createdAt, String updatedBy, Instant updatedAt) {
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
