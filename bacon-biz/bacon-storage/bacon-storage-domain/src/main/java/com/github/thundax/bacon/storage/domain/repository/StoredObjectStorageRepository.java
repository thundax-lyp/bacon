package com.github.thundax.bacon.storage.domain.repository;

import com.github.thundax.bacon.storage.domain.model.entity.MultipartUploadSession;
import com.github.thundax.bacon.storage.domain.model.entity.MultipartUploadPart;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObject;
import com.github.thundax.bacon.storage.domain.model.valueobject.MultipartUploadStorageSession;
import com.github.thundax.bacon.storage.domain.model.valueobject.StoredObjectStorageResult;

import java.io.InputStream;
import java.util.List;

public interface StoredObjectStorageRepository {

    StoredObjectStorageResult upload(String category, String originalFilename, String contentType, InputStream inputStream);

    MultipartUploadStorageSession initMultipartUpload(String category, String originalFilename, String contentType);

    String uploadPart(MultipartUploadSession session, Integer partNumber, Long size, InputStream inputStream);

    StoredObjectStorageResult completeMultipartUpload(MultipartUploadSession session, List<MultipartUploadPart> parts);

    void abortMultipartUpload(MultipartUploadSession session);

    void delete(StoredObject storedObject);
}
