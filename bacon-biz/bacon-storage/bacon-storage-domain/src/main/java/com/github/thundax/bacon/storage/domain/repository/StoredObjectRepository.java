package com.github.thundax.bacon.storage.domain.repository;

import com.github.thundax.bacon.storage.domain.model.entity.StoredObject;
import com.github.thundax.bacon.storage.domain.model.valueobject.StoredObjectPageQuery;
import com.github.thundax.bacon.storage.domain.model.valueobject.StoredObjectPageResult;

import java.util.List;
import java.util.Optional;

public interface StoredObjectRepository {

    StoredObject save(StoredObject storedObject);

    Optional<StoredObject> findById(Long objectId);

    List<StoredObject> listByObjectStatus(String objectStatus, int limit);

    StoredObjectPageResult pageObjects(StoredObjectPageQuery query);
}
