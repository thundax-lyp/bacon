package com.github.thundax.bacon.common.id.core;

import com.github.thundax.bacon.common.id.domain.ResourceId;
import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.common.id.domain.UserCredentialId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.common.id.domain.UserIdentityId;

public class DefaultIds implements Ids {

    private static final String RESOURCE_ID_BIZ_TAG = "resource-id";
    private static final String USER_CREDENTIAL_ID_BIZ_TAG = "user-credential-id";
    private static final String USER_ID_BIZ_TAG = "user-id";
    private static final String USER_IDENTITY_ID_BIZ_TAG = "user-identity-id";
    private static final String STORED_OBJECT_ID_BIZ_TAG = "stored-object-id";

    private final IdGenerator idGenerator;

    public DefaultIds(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Override
    public ResourceId resourceId() {
        return ResourceId.of(idGenerator.nextId(RESOURCE_ID_BIZ_TAG));
    }

    @Override
    public UserCredentialId userCredentialId() {
        return UserCredentialId.of(idGenerator.nextId(USER_CREDENTIAL_ID_BIZ_TAG));
    }

    @Override
    public UserId userId() {
        return UserId.of(idGenerator.nextId(USER_ID_BIZ_TAG));
    }

    @Override
    public UserIdentityId userIdentityId() {
        return UserIdentityId.of(idGenerator.nextId(USER_IDENTITY_ID_BIZ_TAG));
    }

    @Override
    public StoredObjectId storedObjectId() {
        return StoredObjectId.of(idGenerator.nextId(STORED_OBJECT_ID_BIZ_TAG));
    }
}
