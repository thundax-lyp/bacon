package com.github.thundax.bacon.storage.domain.repository;

import com.github.thundax.bacon.storage.domain.model.entity.MultipartUploadPart;
import com.github.thundax.bacon.storage.domain.model.entity.MultipartUploadSession;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObject;
import com.github.thundax.bacon.storage.domain.model.valueobject.MultipartUploadStorageSession;
import com.github.thundax.bacon.storage.domain.model.valueobject.StoredObjectStorageResult;
import java.io.InputStream;
import java.util.List;

public interface StoredObjectStorageRepository {

    StoredObjectStorageResult insert(
            String category, String originalFilename, String contentType, InputStream inputStream);

    MultipartUploadStorageSession insertMultipartUpload(String category, String originalFilename, String contentType);

    String insertPart(MultipartUploadSession session, Integer partNumber, Long size, InputStream inputStream);

    StoredObjectStorageResult update(MultipartUploadSession session, List<MultipartUploadPart> parts);

    void delete(MultipartUploadSession session);

    void delete(StoredObject storedObject);
}
