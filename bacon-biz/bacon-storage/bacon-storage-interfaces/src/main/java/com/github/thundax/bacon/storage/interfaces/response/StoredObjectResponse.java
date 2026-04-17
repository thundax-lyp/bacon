package com.github.thundax.bacon.storage.interfaces.response;

import com.github.thundax.bacon.storage.application.dto.StoredObjectDTO;
import java.time.Instant;

public record StoredObjectResponse(
        String storedObjectNo,
        String storageType,
        String bucketName,
        String objectKey,
        String originalFilename,
        String contentType,
        Long size,
        String accessEndpoint,
        String objectStatus,
        String referenceStatus,
        Instant createdAt) {

    public static StoredObjectResponse from(StoredObjectDTO dto) {
        return new StoredObjectResponse(
                dto.getStoredObjectNo(),
                dto.getStorageType(),
                dto.getBucketName(),
                dto.getObjectKey(),
                dto.getOriginalFilename(),
                dto.getContentType(),
                dto.getSize(),
                dto.getAccessEndpoint(),
                dto.getObjectStatus(),
                dto.getReferenceStatus(),
                dto.getCreatedAt());
    }
}
