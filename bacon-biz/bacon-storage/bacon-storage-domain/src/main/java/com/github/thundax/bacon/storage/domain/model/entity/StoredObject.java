package com.github.thundax.bacon.storage.domain.model.entity;

import lombok.Getter;

import java.time.Instant;

/**
 * 存储对象领域实体。
 */
@Getter
public class StoredObject {

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

    public void markReferenced() {
        this.referenceStatus = "REFERENCED";
        this.updatedAt = Instant.now();
    }

    public void markUnreferenced() {
        this.referenceStatus = "UNREFERENCED";
        this.updatedAt = Instant.now();
    }

    public void markDeleted() {
        this.objectStatus = "DELETED";
        this.updatedAt = Instant.now();
    }

    public void refreshAccessUrl(String accessUrl) {
        this.accessUrl = accessUrl;
        this.updatedAt = Instant.now();
    }
}
