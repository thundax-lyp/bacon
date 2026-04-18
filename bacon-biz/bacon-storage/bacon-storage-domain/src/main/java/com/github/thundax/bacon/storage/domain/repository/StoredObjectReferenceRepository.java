package com.github.thundax.bacon.storage.domain.repository;

import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObjectReference;

public interface StoredObjectReferenceRepository {

    boolean insert(StoredObjectReference storedObjectReference);

    boolean delete(StoredObjectId objectId, String ownerType, String ownerId);

    boolean exists(StoredObjectId objectId, String ownerType, String ownerId);

    boolean exists(StoredObjectId objectId);
}
