package com.github.thundax.bacon.storage.domain.repository;

import com.github.thundax.bacon.storage.domain.model.entity.MultipartUploadPart;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObject;
import com.github.thundax.bacon.storage.domain.model.valueobject.StoredObjectStorageResult;

import java.io.InputStream;
import java.util.List;

public interface StoredObjectStorageRepository {

    StoredObjectStorageResult upload(String category, String originalFilename, String contentType, InputStream inputStream);

    String uploadPart(String uploadId, Integer partNumber, InputStream inputStream);

    StoredObjectStorageResult completeMultipartUpload(String uploadId, String category, String originalFilename,
                                                      List<MultipartUploadPart> parts);

    void abortMultipartUpload(String uploadId);

    void delete(StoredObject storedObject);
}
