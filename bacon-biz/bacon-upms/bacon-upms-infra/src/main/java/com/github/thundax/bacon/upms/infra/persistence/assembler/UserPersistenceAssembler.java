package com.github.thundax.bacon.upms.infra.persistence.assembler;

import com.github.thundax.bacon.upms.domain.model.entity.User;
import com.github.thundax.bacon.upms.domain.model.entity.UserCredential;
import com.github.thundax.bacon.upms.domain.model.entity.UserIdentity;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialFactorLevel;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialStatus;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialType;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityStatus;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityType;
import com.github.thundax.bacon.upms.domain.model.enums.UserStatus;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.UserCredentialDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.UserDO;
import com.github.thundax.bacon.upms.infra.persistence.dataobject.UserIdentityDO;

public final class UserPersistenceAssembler {

    private UserPersistenceAssembler() {}

    public static UserDO toDataObject(User user) {
        return new UserDO(
                user.getId(),
                user.getTenantId(),
                user.getName(),
                user.getAvatarObjectId(),
                user.getDepartmentId(),
                user.getStatus().value(),
                false);
    }

    public static User toDomain(UserDO dataObject) {
        return User.reconstruct(
                dataObject.getId(),
                dataObject.getTenantId(),
                dataObject.getName(),
                dataObject.getAvatarObjectId(),
                dataObject.getDepartmentId(),
                UserStatus.valueOf(dataObject.getStatus()));
    }

    public static UserIdentityDO toDataObject(UserIdentity userIdentity) {
        return new UserIdentityDO(
                userIdentity.getId(),
                userIdentity.getTenantId(),
                userIdentity.getUserId(),
                userIdentity.getIdentityType() == null ? null : userIdentity.getIdentityType().value(),
                userIdentity.getIdentityValue(),
                userIdentity.getStatus() == null ? null : userIdentity.getStatus().value());
    }

    public static UserIdentity toDomain(UserIdentityDO dataObject) {
        return UserIdentity.reconstruct(
                dataObject.getId(),
                dataObject.getTenantId(),
                dataObject.getUserId(),
                UserIdentityType.from(dataObject.getIdentityType()),
                dataObject.getIdentityValue(),
                UserIdentityStatus.from(dataObject.getStatus()));
    }

    public static UserCredentialDO toDataObject(UserCredential userCredential) {
        return new UserCredentialDO(
                userCredential.getId(),
                userCredential.getTenantId(),
                userCredential.getUserId(),
                userCredential.getIdentityId(),
                userCredential.getCredentialType() == null ? null : userCredential.getCredentialType().value(),
                userCredential.getFactorLevel() == null ? null : userCredential.getFactorLevel().value(),
                userCredential.getCredentialValue(),
                userCredential.getStatus().value(),
                userCredential.isNeedChangePassword(),
                userCredential.getFailedCount(),
                userCredential.getFailedLimit(),
                userCredential.getLockReason(),
                UpmsPersistenceAssemblerSupport.toLocalDateTime(userCredential.getLockedUntil()),
                UpmsPersistenceAssemblerSupport.toLocalDateTime(userCredential.getExpiresAt()),
                UpmsPersistenceAssemblerSupport.toLocalDateTime(userCredential.getLastVerifiedAt()));
    }

    public static UserCredential toDomain(UserCredentialDO dataObject) {
        return UserCredential.reconstruct(
                dataObject.getId(),
                dataObject.getTenantId(),
                dataObject.getUserId(),
                dataObject.getIdentityId(),
                UserCredentialType.from(dataObject.getCredentialType()),
                UserCredentialFactorLevel.from(dataObject.getFactorLevel()),
                dataObject.getCredentialValue(),
                UserCredentialStatus.from(dataObject.getStatus()),
                Boolean.TRUE.equals(dataObject.getNeedChangePassword()),
                dataObject.getFailedCount() == null ? 0 : dataObject.getFailedCount(),
                dataObject.getFailedLimit() == null ? 0 : dataObject.getFailedLimit(),
                dataObject.getLockReason(),
                UpmsPersistenceAssemblerSupport.toInstant(dataObject.getLockedUntil()),
                UpmsPersistenceAssemblerSupport.toInstant(dataObject.getExpiresAt()),
                UpmsPersistenceAssemblerSupport.toInstant(dataObject.getLastVerifiedAt()));
    }
}
