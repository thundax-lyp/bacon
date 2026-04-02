package com.github.thundax.bacon.storage.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.storage.domain.model.enums.StorageType;
import lombok.Getter;

import java.time.Instant;

/**
 * 存储对象领域实体。
 */
@Getter
public class StoredObject {

    public static final String OBJECT_STATUS_ACTIVE = "ACTIVE";
    public static final String OBJECT_STATUS_DELETING = "DELETING";
    public static final String OBJECT_STATUS_DELETED = "DELETED";
    public static final String REFERENCE_STATUS_REFERENCED = "REFERENCED";
    public static final String REFERENCE_STATUS_UNREFERENCED = "UNREFERENCED";

    /** 主键。 */
    private StoredObjectId id;
    /** 所属租户业务键。 */
    private TenantId tenantId;
    /** 底层存储类型。 */
    private StorageType storageType;
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
    /** 由 Storage 派生的对象访问端点，仅用于展示/下载，不作为业务主数据持久化。 */
    private String accessEndpoint;
    /** 对象状态。 */
    private String objectStatus;
    /** 引用状态。 */
    private String referenceStatus;
    /** 创建人。 */
    private String createdBy;
    /** 创建时间。 */
    private Instant createdAt;
    /** 更新人。 */
    private String updatedBy;
    /** 更新时间。 */
    private Instant updatedAt;

    public StoredObject(StoredObjectId id, TenantId tenantId, StorageType storageType, String bucketName, String objectKey,
                        String originalFilename, String contentType, Long size, String accessEndpoint, String objectStatus,
                        String referenceStatus, String createdBy, Instant createdAt, String updatedBy, Instant updatedAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.storageType = storageType;
        this.bucketName = bucketName;
        this.objectKey = objectKey;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.size = size;
        this.accessEndpoint = accessEndpoint;
        this.objectStatus = objectStatus;
        this.referenceStatus = referenceStatus;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedBy = updatedBy;
        this.updatedAt = updatedAt;
    }

    public static StoredObject newUploadedObject(TenantId tenantId, StorageType storageType, String bucketName, String objectKey,
                                                 String originalFilename, String contentType, Long size, String accessEndpoint,
                                                 String createdBy) {
        Instant now = Instant.now();
        return new StoredObject(null, tenantId, storageType, bucketName, objectKey, originalFilename, contentType, size,
                accessEndpoint, OBJECT_STATUS_ACTIVE, REFERENCE_STATUS_UNREFERENCED, createdBy, now, createdBy, now);
    }

    public boolean isDeleted() {
        return OBJECT_STATUS_DELETED.equals(this.objectStatus);
    }

    public boolean isDeleting() {
        return OBJECT_STATUS_DELETING.equals(this.objectStatus);
    }

    public boolean isReferenced() {
        return REFERENCE_STATUS_REFERENCED.equals(this.referenceStatus);
    }

    public void markReferenced() {
        ensureActive("Referenced");
        this.referenceStatus = REFERENCE_STATUS_REFERENCED;
        this.updatedAt = Instant.now();
    }

    public void markUnreferenced() {
        ensureActive("Unreferenced");
        this.referenceStatus = REFERENCE_STATUS_UNREFERENCED;
        this.updatedAt = Instant.now();
    }

    public void markDeleting() {
        ensureActive("Deleting");
        this.objectStatus = OBJECT_STATUS_DELETING;
        this.updatedAt = Instant.now();
    }

    public void markDeleted() {
        if (!isDeleting()) {
            throw new IllegalStateException("Deleted object must be in DELETING status first");
        }
        this.objectStatus = OBJECT_STATUS_DELETED;
        this.updatedAt = Instant.now();
    }

    public void refreshAccessEndpoint(String accessEndpoint) {
        this.accessEndpoint = accessEndpoint;
        this.updatedAt = Instant.now();
    }

    private void ensureActive(String action) {
        if (!OBJECT_STATUS_ACTIVE.equals(this.objectStatus)) {
            throw new IllegalStateException(action + " object must be in ACTIVE status");
        }
    }
}
