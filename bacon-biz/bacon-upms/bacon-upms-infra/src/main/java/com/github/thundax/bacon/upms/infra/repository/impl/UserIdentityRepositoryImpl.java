package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.UserIdentity;
import com.github.thundax.bacon.upms.domain.model.enums.UserIdentityType;
import com.github.thundax.bacon.upms.domain.repository.UserIdentityRepository;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
public class UserIdentityRepositoryImpl implements UserIdentityRepository {

    private final UserIdentityPersistenceSupport support;

    public UserIdentityRepositoryImpl(UserIdentityPersistenceSupport support) {
        this.support = support;
    }

    @Override
    public Optional<UserIdentity> findIdentity(UserIdentityType identityType, String identityValue) {
        return support.findIdentity(identityType, identityValue);
    }

    @Override
    public Optional<UserIdentity> findIdentityByUserId(UserId userId, UserIdentityType identityType) {
        return support.findIdentityByUserId(userId, identityType);
    }
}
