package com.github.thundax.bacon.storage.domain.repository;

import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObjectReference;

public interface StoredObjectReferenceRepository {

    boolean saveIfAbsent(StoredObjectReference storedObjectReference);

    boolean deleteByObjectIdAndOwner(StoredObjectId objectId, String ownerType, String ownerId);

    boolean existsByObjectIdAndOwner(StoredObjectId objectId, String ownerType, String ownerId);

    boolean existsByObjectId(StoredObjectId objectId);
}
