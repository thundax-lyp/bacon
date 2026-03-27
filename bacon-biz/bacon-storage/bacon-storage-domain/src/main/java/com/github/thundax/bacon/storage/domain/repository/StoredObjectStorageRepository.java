package com.github.thundax.bacon.storage.domain.repository;

import com.github.thundax.bacon.storage.api.dto.UploadObjectCommand;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObject;
import com.github.thundax.bacon.storage.domain.model.valueobject.StoredObjectStorageResult;

public interface StoredObjectStorageRepository {

    StoredObjectStorageResult upload(UploadObjectCommand command);

    void delete(StoredObject storedObject);
}
