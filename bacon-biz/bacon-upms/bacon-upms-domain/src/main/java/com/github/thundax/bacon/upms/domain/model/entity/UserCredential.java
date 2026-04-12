package com.github.thundax.bacon.upms.domain.model.entity;

import com.github.thundax.bacon.auth.domain.model.valueobject.UserCredentialId;
import com.github.thundax.bacon.auth.domain.model.valueobject.UserIdentityId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialFactorLevel;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialStatus;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialType;
import java.time.Instant;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 用户认证凭据领域实体。
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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

    public static UserCredential create(
            UserCredentialId id,
            TenantId tenantId,
            UserId userId,
            UserIdentityId identityId,
            UserCredentialType credentialType,
            UserCredentialFactorLevel factorLevel,
            String credentialValue,
            UserCredentialStatus status,
            boolean needChangePassword,
            int failedCount,
            int failedLimit,
            String lockReason,
            Instant lockedUntil,
            Instant expiresAt,
            Instant lastVerifiedAt) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(credentialType, "credentialType must not be null");
        Objects.requireNonNull(factorLevel, "factorLevel must not be null");
        Objects.requireNonNull(credentialValue, "credentialValue must not be null");
        Objects.requireNonNull(status, "status must not be null");
        return new UserCredential(
                id,
                tenantId,
                userId,
                identityId,
                credentialType,
                factorLevel,
                credentialValue,
                status,
                needChangePassword,
                failedCount,
                failedLimit,
                lockReason,
                lockedUntil,
                expiresAt,
                lastVerifiedAt);
    }

    public static UserCredential reconstruct(
            UserCredentialId id,
            TenantId tenantId,
            UserId userId,
            UserIdentityId identityId,
            UserCredentialType credentialType,
            UserCredentialFactorLevel factorLevel,
            String credentialValue,
            UserCredentialStatus status,
            boolean needChangePassword,
            int failedCount,
            int failedLimit,
            String lockReason,
            Instant lockedUntil,
            Instant expiresAt,
            Instant lastVerifiedAt) {
        return new UserCredential(
                id,
                tenantId,
                userId,
                identityId,
                credentialType,
                factorLevel,
                credentialValue,
                status,
                needChangePassword,
                failedCount,
                failedLimit,
                lockReason,
                lockedUntil,
                expiresAt,
                lastVerifiedAt);
    }
}
