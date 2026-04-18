package com.github.thundax.bacon.upms.domain.model.entity;

import com.github.thundax.bacon.auth.domain.model.valueobject.UserCredentialId;
import com.github.thundax.bacon.auth.domain.model.valueobject.UserIdentityId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.exception.UserCredentialErrorCode;
import com.github.thundax.bacon.upms.domain.exception.UpmsDomainException;
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

    public static UserCredential createPassword(
            UserCredentialId id,
            UserId userId,
            UserIdentityId identityId,
            String encodedPassword,
            boolean needChangePassword,
            int failedLimit,
            Instant expiresAt) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(identityId, "identityId must not be null");
        Objects.requireNonNull(encodedPassword, "encodedPassword must not be null");
        return new UserCredential(
                id,
                userId,
                identityId,
                UserCredentialType.PASSWORD,
                UserCredentialFactorLevel.PRIMARY,
                encodedPassword,
                UserCredentialStatus.ACTIVE,
                needChangePassword,
                0,
                failedLimit,
                null,
                null,
                expiresAt,
                null);
    }

    public static UserCredential create(
            UserCredentialId id,
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
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(credentialType, "credentialType must not be null");
        Objects.requireNonNull(factorLevel, "factorLevel must not be null");
        Objects.requireNonNull(credentialValue, "credentialValue must not be null");
        Objects.requireNonNull(status, "status must not be null");
        return new UserCredential(
                id,
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

    public void assertVerifiable(Instant now) {
        Objects.requireNonNull(now, "now must not be null");
        if (isLocked(now)) {
            throw new UpmsDomainException(UserCredentialErrorCode.USER_CREDENTIAL_LOCKED);
        }
        if (status != UserCredentialStatus.ACTIVE) {
            throw new UpmsDomainException(UserCredentialErrorCode.USER_CREDENTIAL_NOT_ACTIVE);
        }
        if (expiresAt != null && !expiresAt.isAfter(now)) {
            throw new UpmsDomainException(UserCredentialErrorCode.USER_CREDENTIAL_EXPIRED);
        }
    }

    public boolean isExpired(Instant now) {
        Objects.requireNonNull(now, "now must not be null");
        return expiresAt != null && !expiresAt.isAfter(now);
    }

    public boolean isLocked(Instant now) {
        Objects.requireNonNull(now, "now must not be null");
        if (lockedUntil != null && lockedUntil.isAfter(now)) {
            return true;
        }
        return status == UserCredentialStatus.LOCKED && lockedUntil == null;
    }

    public boolean isActive() {
        return status == UserCredentialStatus.ACTIVE;
    }

    public boolean isDisabled() {
        return status == UserCredentialStatus.DISABLED;
    }

    public boolean isPasswordCredential() {
        return credentialType == UserCredentialType.PASSWORD;
    }

    public boolean needsPasswordChange() {
        return needChangePassword;
    }

    public boolean matchesFactorLevel(UserCredentialFactorLevel level) {
        Objects.requireNonNull(level, "level must not be null");
        return factorLevel == level;
    }

    public boolean canBeUsedForAuthentication(Instant now) {
        Objects.requireNonNull(now, "now must not be null");
        return isActive() && !isLocked(now) && !isExpired(now);
    }

    public void bindIdentity(UserIdentityId identityId) {
        Objects.requireNonNull(identityId, "identityId must not be null");
        this.identityId = identityId;
    }

    public void replaceCredentialValue(String newValue) {
        Objects.requireNonNull(newValue, "newValue must not be null");
        this.credentialValue = newValue;
    }

    public void replacePassword(String newEncodedPassword, boolean needChangePassword, Instant expiresAt) {
        Objects.requireNonNull(newEncodedPassword, "newEncodedPassword must not be null");
        this.credentialValue = newEncodedPassword;
        this.status = UserCredentialStatus.ACTIVE;
        this.needChangePassword = needChangePassword;
        this.failedCount = 0;
        this.lockReason = null;
        this.lockedUntil = null;
        this.expiresAt = expiresAt;
    }

    public void markVerified(Instant now) {
        Objects.requireNonNull(now, "now must not be null");
        this.status = UserCredentialStatus.ACTIVE;
        this.failedCount = 0;
        this.lockReason = null;
        this.lockedUntil = null;
        this.lastVerifiedAt = now;
    }

    public void markFailed(String reason, Instant now) {
        Objects.requireNonNull(reason, "reason must not be null");
        Objects.requireNonNull(now, "now must not be null");
        this.failedCount += 1;
        this.lockReason = reason;
        if (failedLimit > 0 && failedCount >= failedLimit) {
            this.status = UserCredentialStatus.LOCKED;
        }
    }

    public void lock(String reason, Instant until) {
        Objects.requireNonNull(reason, "reason must not be null");
        this.status = UserCredentialStatus.LOCKED;
        this.lockReason = reason;
        this.lockedUntil = until;
    }

    public void unlock() {
        this.status = UserCredentialStatus.ACTIVE;
        this.lockReason = null;
        this.lockedUntil = null;
    }

    public void activate() {
        this.status = UserCredentialStatus.ACTIVE;
    }

    public void disable() {
        this.status = UserCredentialStatus.DISABLED;
    }

    public void expireAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void clearExpiry() {
        this.expiresAt = null;
    }

    public void requirePasswordChange() {
        if (credentialType != UserCredentialType.PASSWORD) {
            throw new UpmsDomainException(
                    UserCredentialErrorCode.USER_CREDENTIAL_PASSWORD_CHANGE_NOT_SUPPORTED);
        }
        this.needChangePassword = true;
    }

    public void clearPasswordChangeRequirement() {
        if (credentialType != UserCredentialType.PASSWORD) {
            throw new UpmsDomainException(
                    UserCredentialErrorCode.USER_CREDENTIAL_PASSWORD_CHANGE_NOT_SUPPORTED);
        }
        this.needChangePassword = false;
    }
}
