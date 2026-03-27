package com.github.thundax.bacon.storage.domain.model.entity;

import lombok.Getter;

import java.time.Instant;

/**
 * 存储对象领域实体。
 */
@Getter
public class StoredObject {

    public static final String OBJECT_STATUS_ACTIVE = "ACTIVE";
    public static final String OBJECT_STATUS_DELETED = "DELETED";
    public static final String REFERENCE_STATUS_REFERENCED = "REFERENCED";
    public static final String REFERENCE_STATUS_UNREFERENCED = "UNREFERENCED";

    /** 主键。 */
    private Long id;
    /** 所属租户业务键。 */
    private String tenantId;
    /** 底层存储类型。 */
    private String storageType;
    /** 存储桶或本地逻辑目录。 */
    private String bucketName;
    /** 底层对象键，全局唯一。 */
    private String objectKey;
    /** 原始文件名。 */
    private String originalFilename;
    /** 内容类型。 */
    private String contentType;
    /** 文件大小，字节。 */
    private Long size;
    /** 当前访问地址。 */
    private String accessUrl;
    /** 对象状态。 */
    private String objectStatus;
    /** 引用状态。 */
    private String referenceStatus;
    /** 创建人。 */
    private Long createdBy;
    /** 创建时间。 */
    private Instant createdAt;
    /** 更新人。 */
    private Long updatedBy;
    /** 更新时间。 */
    private Instant updatedAt;

    public StoredObject(Long id, String tenantId, String storageType, String bucketName, String objectKey,
                        String originalFilename, String contentType, Long size, String accessUrl, String objectStatus,
                        String referenceStatus, Long createdBy, Instant createdAt, Long updatedBy, Instant updatedAt) {
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
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }

    public static StoredObject newUploadedObject(String tenantId, String storageType, String bucketName, String objectKey,
                                                 String originalFilename, String contentType, Long size, String accessUrl,
                                                 Long createdBy) {
        Instant now = Instant.now();
        return new StoredObject(null, tenantId, storageType, bucketName, objectKey, originalFilename, contentType, size,
                accessUrl, OBJECT_STATUS_ACTIVE, REFERENCE_STATUS_UNREFERENCED, createdBy, now, createdBy, now);
    }

    public boolean isDeleted() {
        return OBJECT_STATUS_DELETED.equals(this.objectStatus);
    }

    public boolean isReferenced() {
        return REFERENCE_STATUS_REFERENCED.equals(this.referenceStatus);
    }

    public void markReferenced() {
        this.referenceStatus = REFERENCE_STATUS_REFERENCED;
        this.updatedAt = Instant.now();
    }

    public void markUnreferenced() {
        this.referenceStatus = REFERENCE_STATUS_UNREFERENCED;
        this.updatedAt = Instant.now();
    }

    public void markDeleted() {
        this.objectStatus = OBJECT_STATUS_DELETED;
        this.updatedAt = Instant.now();
    }

    public void refreshAccessUrl(String accessUrl) {
        this.accessUrl = accessUrl;
        this.updatedAt = Instant.now();
    }
}
