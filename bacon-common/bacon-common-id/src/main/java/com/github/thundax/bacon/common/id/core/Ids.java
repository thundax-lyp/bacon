package com.github.thundax.bacon.common.id.core;

import com.github.thundax.bacon.common.id.domain.ResourceId;
import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.common.id.domain.UserCredentialId;
import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.common.id.domain.UserIdentityId;

public interface Ids {

    ResourceId resourceId();

    UserCredentialId userCredentialId();

    UserId userId();

    UserIdentityId userIdentityId();

    StoredObjectId storedObjectId();
}
