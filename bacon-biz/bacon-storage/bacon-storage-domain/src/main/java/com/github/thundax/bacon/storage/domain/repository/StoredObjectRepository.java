package com.github.thundax.bacon.storage.domain.repository;

import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObject;
import com.github.thundax.bacon.storage.domain.model.enums.StoredObjectStatus;

import java.util.List;
import java.util.Optional;

public interface StoredObjectRepository {

    StoredObject save(StoredObject storedObject);

    Optional<StoredObject> findById(StoredObjectId objectId);

    List<StoredObject> listByObjectStatus(StoredObjectStatus objectStatus, int limit);

    List<StoredObject> pageObjects(String tenantId, String storageType, String objectStatus,
                                   String referenceStatus, String originalFilename, String objectKey,
                                   int offset, int limit);

    long countObjects(String tenantId, String storageType, String objectStatus, String referenceStatus,
                      String originalFilename, String objectKey);
}
