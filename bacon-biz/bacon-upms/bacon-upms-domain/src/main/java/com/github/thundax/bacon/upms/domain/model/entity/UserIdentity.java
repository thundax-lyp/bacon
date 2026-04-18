package com.github.thundax.bacon.upms.domain.model.entity;

import com.github.thundax.bacon.auth.domain.model.valueobject.UserIdentityId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.exception.UserIdentityDomainException;
import com.github.thundax.bacon.upms.domain.exception.UserIdentityErrorCode;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityStatus;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityType;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 用户身份标识领域实体。
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UserIdentity {

    /** 身份标识主键。 */
    private UserIdentityId id;
    /** 关联用户主键。 */
    private UserId userId;
    /** 身份标识类型。 */
    private UserIdentityType identityType;
    /** 身份标识值。 */
    private String identityValue;
    /** 身份状态。 */
    private UserIdentityStatus status;

    public static UserIdentity create(
            UserIdentityId id,
            UserId userId,
            UserIdentityType identityType,
            String identityValue,
            UserIdentityStatus status) {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(identityType, "identityType must not be null");
        Objects.requireNonNull(identityValue, "identityValue must not be null");
        Objects.requireNonNull(status, "status must not be null");
        return new UserIdentity(id, userId, identityType, identityValue, status);
    }

    public static UserIdentity reconstruct(
            UserIdentityId id,
            UserId userId,
            UserIdentityType identityType,
            String identityValue,
            UserIdentityStatus status) {
        return new UserIdentity(id, userId, identityType, identityValue, status);
    }

    public void assertUsable() {
        if (status != UserIdentityStatus.ACTIVE) {
            throw new UserIdentityDomainException(UserIdentityErrorCode.USER_IDENTITY_NOT_USABLE);
        }
    }

    public void activate() {
        this.status = UserIdentityStatus.ACTIVE;
    }

    public void disable() {
        this.status = UserIdentityStatus.DISABLED;
    }

    public void revoke() {
        this.status = UserIdentityStatus.DISABLED;
    }

    public void changeIdentityValue(String newValue) {
        Objects.requireNonNull(newValue, "newValue must not be null");
        this.identityValue = newValue;
    }

    public boolean matches(String value) {
        Objects.requireNonNull(value, "value must not be null");
        return Objects.equals(this.identityValue, value);
    }

    public boolean isEmail() {
        return identityType == UserIdentityType.EMAIL;
    }

    public boolean isPhone() {
        return identityType == UserIdentityType.PHONE;
    }

    public boolean isUsername() {
        return identityType == UserIdentityType.ACCOUNT;
    }

    public boolean canLogin() {
        return status == UserIdentityStatus.ACTIVE;
    }

    public void assertLoginAllowed() {
        if (!canLogin()) {
            throw new UserIdentityDomainException(UserIdentityErrorCode.USER_IDENTITY_LOGIN_NOT_ALLOWED);
        }
    }
}
