package com.github.thundax.bacon.storage.domain.model.entity;

import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import com.github.thundax.bacon.storage.domain.model.enums.StorageType;
import com.github.thundax.bacon.storage.domain.model.enums.StoredObjectReferenceStatus;
import com.github.thundax.bacon.storage.domain.model.enums.StoredObjectStatus;
import com.github.thundax.bacon.storage.domain.model.valueobject.StoredObjectNo;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 存储对象领域实体。
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StoredObject {

    /** 主键。 */
    private StoredObjectId id;
    /** 存储对象外部编号。 */
    private StoredObjectNo storedObjectNo;
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

    public static StoredObject create(
            StoredObjectId id,
            StoredObjectNo storedObjectNo,
            StorageType storageType,
            String bucketName,
            String objectKey,
            String originalFilename,
            String contentType,
            Long size,
            String accessEndpoint) {
        return new StoredObject(
                id,
                storedObjectNo,
                storageType,
                bucketName,
                objectKey,
                originalFilename,
                contentType,
                size,
                accessEndpoint,
                StoredObjectStatus.ACTIVE,
                StoredObjectReferenceStatus.UNREFERENCED);
    }

    public static StoredObject reconstruct(
            StoredObjectId id,
            StoredObjectNo storedObjectNo,
            StorageType storageType,
            String bucketName,
            String objectKey,
            String originalFilename,
            String contentType,
            Long size,
            String accessEndpoint,
            StoredObjectStatus objectStatus,
            StoredObjectReferenceStatus referenceStatus) {
        return new StoredObject(
                id,
                storedObjectNo,
                storageType,
                bucketName,
                objectKey,
                originalFilename,
                contentType,
                size,
                accessEndpoint,
                objectStatus,
                referenceStatus);
    }

    public static StoredObject create(
            StoredObjectId id,
            StorageType storageType,
            String bucketName,
            String objectKey,
            String originalFilename,
            String contentType,
            Long size,
            String accessEndpoint) {
        return create(id, null, storageType, bucketName, objectKey, originalFilename, contentType, size, accessEndpoint);
    }

    public static StoredObject reconstruct(
            StoredObjectId id,
            StorageType storageType,
            String bucketName,
            String objectKey,
            String originalFilename,
            String contentType,
            Long size,
            String accessEndpoint,
            StoredObjectStatus objectStatus,
            StoredObjectReferenceStatus referenceStatus) {
        return reconstruct(
                id,
                null,
                storageType,
                bucketName,
                objectKey,
                originalFilename,
                contentType,
                size,
                accessEndpoint,
                objectStatus,
                referenceStatus);
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
    }

    public void markUnreferenced() {
        ensureActive("Unreferenced");
        this.referenceStatus = StoredObjectReferenceStatus.UNREFERENCED;
    }

    public void markDeleting() {
        ensureActive("Deleting");
        this.objectStatus = StoredObjectStatus.DELETING;
    }

    public void markDeleted() {
        if (!isDeleting()) {
            throw new IllegalStateException("Deleted object must be in DELETING status first");
        }
        this.objectStatus = StoredObjectStatus.DELETED;
    }

    public void refreshAccessEndpoint(String accessEndpoint) {
        this.accessEndpoint = accessEndpoint;
    }

    private void ensureActive(String action) {
        if (StoredObjectStatus.ACTIVE != this.objectStatus) {
            throw new IllegalStateException(action + " object must be in ACTIVE status");
        }
    }
}
