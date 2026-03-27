package com.github.thundax.bacon.storage.domain.model.entity;

import lombok.Getter;

import java.time.Instant;

/**
 * 存储对象领域实体。
 */
@Getter
public class StoredObject {

    private final Long id;
    private final String tenantId;
    private final String storageType;
    private final String bucketName;
    private final String objectKey;
    private final String originalFilename;
    private final String contentType;
    private final Long size;
    private String accessUrl;
    private String objectStatus;
    private String referenceStatus;
    private final Long createdBy;
    private final Instant createdAt;

    public StoredObject(Long id, String tenantId, String storageType, String bucketName, String objectKey,
                        String originalFilename, String contentType, Long size, String accessUrl, String objectStatus,
                        String referenceStatus, Long createdBy, Instant createdAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.storageType = storageType;
        this.bucketName = bucketName;
        this.objectKey = objectKey;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.size = size;
        this.accessUrl = accessUrl;
        this.objectStatus = objectStatus;
        this.referenceStatus = referenceStatus;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }

    public void markReferenced() {
        this.referenceStatus = "REFERENCED";
    }

    public void markUnreferenced() {
        this.referenceStatus = "UNREFERENCED";
    }

    public void markDeleted() {
        this.objectStatus = "DELETED";
    }

    public void refreshAccessUrl(String accessUrl) {
        this.accessUrl = accessUrl;
    }
}
