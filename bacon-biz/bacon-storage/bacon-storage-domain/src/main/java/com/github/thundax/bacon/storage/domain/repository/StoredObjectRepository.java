package com.github.thundax.bacon.storage.domain.repository;

import com.github.thundax.bacon.storage.domain.model.entity.StoredObject;

import java.util.Optional;

public interface StoredObjectRepository {

    StoredObject save(StoredObject storedObject);

    Optional<StoredObject> findById(Long objectId);
}
