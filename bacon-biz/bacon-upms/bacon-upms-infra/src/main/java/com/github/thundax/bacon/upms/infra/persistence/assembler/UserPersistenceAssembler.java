package com.github.thundax.bacon.upms.infra.persistence.assembler;

import com.github.thundax.bacon.auth.domain.model.valueobject.UserCredentialId;
import com.github.thundax.bacon.auth.domain.model.valueobject.UserIdentityId;
import com.github.thundax.bacon.common.core.context.BaconContextHolder;
import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.User;
import com.github.thundax.bacon.upms.domain.model.entity.UserCredential;
import com.github.thundax.bacon.upms.domain.model.entity.UserIdentity;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialFactorLevel;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialStatus;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialType;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityStatus;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityType;
import com.github.thundax.bacon.upms.domain.model.enums.UserStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.UserCredentialDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.UserDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.UserIdentityDO;

public final class UserPersistenceAssembler {

    private UserPersistenceAssembler() {}

    public static UserDO toDataObject(User user) {
        return new UserDO(
                user.getId() == null ? null : user.getId().value(),
                BaconContextHolder.requireTenantId(),
                user.getName(),
                user.getAvatarObjectId() == null
                        ? null
                        : user.getAvatarObjectId().value(),
                user.getDepartmentId() == null ? null : user.getDepartmentId().value(),
                user.getStatus().value(),
                false);
    }

    public static User toDomain(UserDO dataObject) {
        return User.reconstruct(
                dataObject.getId() == null ? null : UserId.of(dataObject.getId()),
                dataObject.getName(),
                dataObject.getAvatarObjectId() == null ? null : StoredObjectId.of(dataObject.getAvatarObjectId()),
                dataObject.getDepartmentId() == null ? null : DepartmentId.of(dataObject.getDepartmentId()),
                UserStatus.valueOf(dataObject.getStatus()));
    }

    public static UserIdentityDO toDataObject(UserIdentity userIdentity) {
        return new UserIdentityDO(
                userIdentity.getId() == null ? null : userIdentity.getId().value(),
                BaconContextHolder.requireTenantId(),
                userIdentity.getUserId() == null
                        ? null
                        : userIdentity.getUserId().value(),
                userIdentity.getIdentityType() == null
                        ? null
                        : userIdentity.getIdentityType().value(),
                userIdentity.getIdentityValue(),
                userIdentity.getStatus() == null
                        ? null
                        : userIdentity.getStatus().value());
    }

    public static UserIdentity toDomain(UserIdentityDO dataObject) {
        return UserIdentity.reconstruct(
                dataObject.getId() == null ? null : UserIdentityId.of(dataObject.getId()),
                dataObject.getUserId() == null ? null : UserId.of(dataObject.getUserId()),
                UserIdentityType.from(dataObject.getIdentityType()),
                dataObject.getIdentityValue(),
                UserIdentityStatus.from(dataObject.getStatus()));
    }

    public static UserCredentialDO toDataObject(UserCredential userCredential) {
        return new UserCredentialDO(
                userCredential.getId() == null ? null : userCredential.getId().value(),
                BaconContextHolder.requireTenantId(),
                userCredential.getUserId() == null
                        ? null
                        : userCredential.getUserId().value(),
                userCredential.getIdentityId() == null
                        ? null
                        : userCredential.getIdentityId().value(),
                userCredential.getCredentialType() == null
                        ? null
                        : userCredential.getCredentialType().value(),
                userCredential.getFactorLevel() == null
                        ? null
                        : userCredential.getFactorLevel().value(),
                userCredential.getCredentialValue(),
                userCredential.getStatus().value(),
                userCredential.isNeedChangePassword(),
                userCredential.getFailedCount(),
                userCredential.getFailedLimit(),
                userCredential.getLockReason(),
                userCredential.getLockedUntil(),
                userCredential.getExpiresAt(),
                userCredential.getLastVerifiedAt());
    }

    public static UserCredential toDomain(UserCredentialDO dataObject) {
        return UserCredential.reconstruct(
                dataObject.getId() == null ? null : UserCredentialId.of(dataObject.getId()),
                dataObject.getUserId() == null ? null : UserId.of(dataObject.getUserId()),
                dataObject.getIdentityId() == null ? null : UserIdentityId.of(dataObject.getIdentityId()),
                UserCredentialType.from(dataObject.getCredentialType()),
                UserCredentialFactorLevel.from(dataObject.getFactorLevel()),
                dataObject.getCredentialValue(),
                UserCredentialStatus.from(dataObject.getStatus()),
                Boolean.TRUE.equals(dataObject.getNeedChangePassword()),
                dataObject.getFailedCount() == null ? 0 : dataObject.getFailedCount(),
                dataObject.getFailedLimit() == null ? 0 : dataObject.getFailedLimit(),
                dataObject.getLockReason(),
                dataObject.getLockedUntil(),
                dataObject.getExpiresAt(),
                dataObject.getLastVerifiedAt());
    }
}
