package com.github.thundax.bacon.upms.infra.repository.impl;

import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.UserCredential;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialType;
import com.github.thundax.bacon.upms.domain.repository.UserCredentialRepository;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
public class UserCredentialRepositoryImpl implements UserCredentialRepository {

    private final UserCredentialPersistenceSupport support;

    public UserCredentialRepositoryImpl(UserCredentialPersistenceSupport support) {
        this.support = support;
    }

    @Override
    public Optional<UserCredential> findCredentialByUserId(UserId userId, UserCredentialType credentialType) {
        return support.findCredentialByUserId(userId, credentialType);
    }
}
