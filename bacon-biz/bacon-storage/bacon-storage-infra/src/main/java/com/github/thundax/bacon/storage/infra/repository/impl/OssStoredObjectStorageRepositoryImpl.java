package com.github.thundax.bacon.storage.infra.repository.impl;

import com.github.thundax.bacon.common.oss.client.ObjectStorageClient;
import com.github.thundax.bacon.common.oss.model.ObjectStoragePart;
import com.github.thundax.bacon.common.oss.model.ObjectStorageWriteResult;
import com.github.thundax.bacon.storage.domain.model.entity.MultipartUploadPart;
import com.github.thundax.bacon.storage.domain.model.entity.MultipartUploadSession;
import com.github.thundax.bacon.storage.domain.model.entity.StoredObject;
import com.github.thundax.bacon.storage.domain.model.enums.StorageType;
import com.github.thundax.bacon.storage.domain.model.valueobject.MultipartUploadStorageSession;
import com.github.thundax.bacon.storage.domain.model.valueobject.StoredObjectStorageResult;
import com.github.thundax.bacon.storage.domain.repository.StoredObjectStorageRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

/**
 * 基于通用 S3 API 客户端的对象存储仓储。
 */
@Repository
@ConditionalOnProperty(name = "bacon.storage.type", havingValue = "OSS")
public class OssStoredObjectStorageRepositoryImpl implements StoredObjectStorageRepository {

    private final ObjectStorageClient objectStorageClient;

    public OssStoredObjectStorageRepositoryImpl(ObjectStorageClient objectStorageClient) {
        this.objectStorageClient = objectStorageClient;
    }

    @Override
    public StoredObjectStorageResult upload(String category, String originalFilename, String contentType,
                                            InputStream inputStream) {
        String objectKey = buildObjectKey(category, originalFilename);
        ObjectStorageWriteResult result = objectStorageClient.putObject(objectKey, contentType, inputStream);
        return toStorageResult(result);
    }

    @Override
    public MultipartUploadStorageSession initMultipartUpload(String category, String originalFilename,
                                                             String contentType) {
        String objectKey = buildObjectKey(category, originalFilename);
        String providerUploadId = objectStorageClient.initiateMultipartUpload(objectKey, contentType);
        return new MultipartUploadStorageSession(objectKey, providerUploadId);
    }

    @Override
    public String uploadPart(MultipartUploadSession session, Integer partNumber, Long size, InputStream inputStream) {
        return objectStorageClient.uploadPart(session.getObjectKey(), session.getProviderUploadId(), partNumber, size,
                inputStream);
    }

    @Override
    public StoredObjectStorageResult completeMultipartUpload(MultipartUploadSession session,
                                                             List<MultipartUploadPart> parts) {
        ObjectStorageWriteResult result = objectStorageClient.completeMultipartUpload(session.getObjectKey(),
                session.getProviderUploadId(), parts.stream()
                        .map(part -> new ObjectStoragePart(part.getPartNumber(), part.getEtag()))
                        .toList());
        return toStorageResult(result);
    }

    @Override
    public void abortMultipartUpload(MultipartUploadSession session) {
        objectStorageClient.abortMultipartUpload(session.getObjectKey(), session.getProviderUploadId());
    }

    @Override
    public void delete(StoredObject storedObject) {
        objectStorageClient.deleteObject(storedObject.getObjectKey());
    }

    private StoredObjectStorageResult toStorageResult(ObjectStorageWriteResult result) {
        return new StoredObjectStorageResult(StorageType.OSS, result.bucketName(), result.objectKey(),
                result.accessEndpoint());
    }

    private String buildObjectKey(String category, String originalFilename) {
        String normalizedCategory = StringUtils.hasText(category) ? category.trim() : "default";
        String extension = extractExtension(originalFilename);
        return normalizedCategory + "/" + UUID.randomUUID() + extension;
    }

    private String extractExtension(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            return "";
        }
        int index = originalFilename.lastIndexOf('.');
        if (index < 0 || index == originalFilename.length() - 1) {
            return "";
        }
        return originalFilename.substring(index);
    }
}
