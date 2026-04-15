package com.github.thundax.bacon.storage.domain.repository;

import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObject;
import com.github.thundax.bacon.storage.domain.model.enums.StorageType;
import com.github.thundax.bacon.storage.domain.model.enums.StoredObjectReferenceStatus;
import com.github.thundax.bacon.storage.domain.model.enums.StoredObjectStatus;
import java.util.List;
import java.util.Optional;

public interface StoredObjectRepository {

    StoredObject insert(StoredObject storedObject);

    StoredObject update(StoredObject storedObject);

    Optional<StoredObject> findById(StoredObjectId objectId);

    List<StoredObject> listByObjectStatus(StoredObjectStatus objectStatus, int limit);

    List<StoredObject> pageObjects(
            StorageType storageType,
            StoredObjectStatus objectStatus,
            StoredObjectReferenceStatus referenceStatus,
            String originalFilename,
            String objectKey,
            int pageNo,
            int pageSize);

    long countObjects(
            StorageType storageType,
            StoredObjectStatus objectStatus,
            StoredObjectReferenceStatus referenceStatus,
            String originalFilename,
            String objectKey);
}
