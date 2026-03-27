package com.github.thundax.bacon.storage.interfaces.response;

import com.github.thundax.bacon.storage.api.dto.StoredObjectDTO;

import java.time.Instant;

/**
 * 存储对象响应。
 */
public record StoredObjectResponse(
        Long id,
        String storageType,
        String bucketName,
        String objectKey,
        String originalFilename,
        String contentType,
        Long size,
        String accessUrl,
        String objectStatus,
        String referenceStatus,
        Instant createdAt) {

    public static StoredObjectResponse from(StoredObjectDTO dto) {
        return new StoredObjectResponse(dto.getId(), dto.getStorageType(), dto.getBucketName(), dto.getObjectKey(),
                dto.getOriginalFilename(), dto.getContentType(), dto.getSize(), dto.getAccessUrl(),
                dto.getObjectStatus(), dto.getReferenceStatus(), dto.getCreatedAt());
    }
}
