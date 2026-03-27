package com.github.thundax.bacon.storage.domain.repository;

import com.github.thundax.bacon.storage.domain.model.entity.StoredObjectReference;

public interface StoredObjectReferenceRepository {

    StoredObjectReference save(StoredObjectReference storedObjectReference);

    void deleteByObjectIdAndOwner(Long objectId, String ownerType, String ownerId);

    boolean existsByObjectIdAndOwner(Long objectId, String ownerType, String ownerId);

    boolean existsByObjectId(Long objectId);
}
