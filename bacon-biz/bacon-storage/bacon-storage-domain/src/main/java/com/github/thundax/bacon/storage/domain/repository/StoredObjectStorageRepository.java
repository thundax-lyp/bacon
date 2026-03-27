package com.github.thundax.bacon.storage.domain.repository;

import com.github.thundax.bacon.storage.domain.model.entity.StoredObject;
import com.github.thundax.bacon.storage.domain.model.valueobject.StoredObjectStorageResult;

import java.io.InputStream;

public interface StoredObjectStorageRepository {

    StoredObjectStorageResult upload(String category, String originalFilename, String contentType, InputStream inputStream);

    void delete(StoredObject storedObject);
}
