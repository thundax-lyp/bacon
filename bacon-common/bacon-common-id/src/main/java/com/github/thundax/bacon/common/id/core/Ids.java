package com.github.thundax.bacon.common.id.core;

import com.github.thundax.bacon.common.id.domain.ResourceId;
import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.common.id.domain.UserId;

public interface Ids {

    ResourceId resourceId();

    UserId userId();

    StoredObjectId storedObjectId();
}
