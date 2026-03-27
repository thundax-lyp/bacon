package com.github.thundax.bacon.storage.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 存储对象传输对象。
 */
@Data
@NoArgsConstructor
public class StoredObjectDTO {

    /** 主键。 */
    private Long id;
    /** 底层存储类型。 */
    private String storageType;
    /** 存储桶或本地逻辑目录。 */
    private String bucketName;
    /** 底层对象键。 */
    private String objectKey;
    /** 原始文件名。 */
    private String originalFilename;
    /** 内容类型。 */
    private String contentType;
    /** 文件大小，字节。 */
    private Long size;
    /** 由 Storage 派生的对象访问端点，仅用于展示/下载，不作为业务主数据持久化。 */
    private String accessEndpoint;
    /** 已废弃，请改用 accessEndpoint。 */
    @Deprecated
    private String accessUrl;
    /** 对象状态。 */
    private String objectStatus;
    /** 引用状态。 */
    private String referenceStatus;
    /** 创建时间。 */
    private Instant createdAt;

    public StoredObjectDTO(Long id, String storageType, String bucketName, String objectKey, String originalFilename,
                           String contentType, Long size, String accessEndpoint, String objectStatus,
                           String referenceStatus, Instant createdAt) {
        this.id = id;
        this.storageType = storageType;
        this.bucketName = bucketName;
        this.objectKey = objectKey;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.size = size;
        this.accessEndpoint = accessEndpoint;
        this.accessUrl = accessEndpoint;
        this.objectStatus = objectStatus;
        this.referenceStatus = referenceStatus;
        this.createdAt = createdAt;
    }

    /** 兼容旧字段读取。 */
    @Deprecated
    public String getAccessUrl() {
        return accessUrl != null ? accessUrl : accessEndpoint;
    }

    /** 兼容旧字段写入。 */
    @Deprecated
    public void setAccessUrl(String accessUrl) {
        this.accessUrl = accessUrl;
        this.accessEndpoint = accessUrl;
    }

    public void setAccessEndpoint(String accessEndpoint) {
        this.accessEndpoint = accessEndpoint;
        this.accessUrl = accessEndpoint;
    }
}
