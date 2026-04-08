package com.github.thundax.bacon.storage.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.storage.domain.model.enums.StoredObjectReferenceStatus;
import com.github.thundax.bacon.storage.domain.model.enums.StoredObjectStatus;
import com.github.thundax.bacon.storage.domain.model.enums.StorageType;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 存储对象领域实体。
 */
@Getter
@AllArgsConstructor
public class StoredObject {

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
    private StoredObjectStatus objectStatus;
    /** 引用状态。 */
    private StoredObjectReferenceStatus referenceStatus;
    /** 创建人。 */
    private String createdBy;
    /** 创建时间。 */
    private Instant createdAt;
    /** 更新人。 */
    private String updatedBy;
    /** 更新时间。 */
    private Instant updatedAt;

    public StoredObject(Long id, Long tenantId, StorageType storageType, String bucketName, String objectKey,
                        String originalFilename, String contentType, Long size, String accessEndpoint,
                        StoredObjectStatus objectStatus, StoredObjectReferenceStatus referenceStatus,
                        String createdBy, Instant createdAt, String updatedBy, Instant updatedAt) {
        this(id == null ? null : StoredObjectId.of(id),
                tenantId == null ? null : TenantId.of(tenantId),
                storageType, bucketName, objectKey, originalFilename, contentType, size, accessEndpoint,
                objectStatus, referenceStatus, createdBy, createdAt, updatedBy, updatedAt);
    }

    public static StoredObject newUploadedObject(TenantId tenantId, StorageType storageType, String bucketName, String objectKey,
                                                 String originalFilename, String contentType, Long size, String accessEndpoint,
                                                 String createdBy) {
        Instant now = Instant.now();
        return new StoredObject(null, tenantId, storageType, bucketName, objectKey, originalFilename, contentType, size,
                accessEndpoint, StoredObjectStatus.ACTIVE, StoredObjectReferenceStatus.UNREFERENCED, createdBy, now,
                createdBy, now);
    }

    public boolean isDeleted() {
        return StoredObjectStatus.DELETED == this.objectStatus;
    }

    public boolean isDeleting() {
        return StoredObjectStatus.DELETING == this.objectStatus;
    }

    public boolean isReferenced() {
        return StoredObjectReferenceStatus.REFERENCED == this.referenceStatus;
    }

    public void markReferenced() {
        ensureActive("Referenced");
        this.referenceStatus = StoredObjectReferenceStatus.REFERENCED;
        this.updatedAt = Instant.now();
    }

    public void markUnreferenced() {
        ensureActive("Unreferenced");
        this.referenceStatus = StoredObjectReferenceStatus.UNREFERENCED;
        this.updatedAt = Instant.now();
    }

    public void markDeleting() {
        ensureActive("Deleting");
        this.objectStatus = StoredObjectStatus.DELETING;
        this.updatedAt = Instant.now();
    }

    public void markDeleted() {
        if (!isDeleting()) {
            throw new IllegalStateException("Deleted object must be in DELETING status first");
        }
        this.objectStatus = StoredObjectStatus.DELETED;
        this.updatedAt = Instant.now();
    }

    public void refreshAccessEndpoint(String accessEndpoint) {
        this.accessEndpoint = accessEndpoint;
        this.updatedAt = Instant.now();
    }

    private void ensureActive(String action) {
        if (StoredObjectStatus.ACTIVE != this.objectStatus) {
            throw new IllegalStateException(action + " object must be in ACTIVE status");
        }
    }
}
