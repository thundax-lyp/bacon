package com.github.thundax.bacon.upms.domain.repository;

import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.entity.UserCredential;
import com.github.thundax.bacon.upms.domain.model.enums.UserCredentialType;
import java.util.Optional;

public interface UserCredentialRepository {

    Optional<UserCredential> findCredentialByUserId(UserId userId, UserCredentialType credentialType);

    UserCredential insert(UserCredential userCredential);

    UserCredential update(UserCredential userCredential);
}
