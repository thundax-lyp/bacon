package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.UserIdentity;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityType;
import java.util.Optional;

public interface UserIdentityRepository {

    Optional<UserIdentity> findIdentity(UserIdentityType identityType, String identityValue);

    Optional<UserIdentity> findIdentityByUserId(UserId userId, UserIdentityType identityType);

    UserIdentity insert(UserIdentity userIdentity);

    UserIdentity update(UserIdentity userIdentity);

    void deleteIdentityByUserIdAndType(UserId userId, UserIdentityType identityType);
}
