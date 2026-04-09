package com.github.thundax.bacon.storage.api.dto;

import com.github.thundax.bacon.common.id.domain.StoredObjectId;
import java.time.Instant;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 存储对象传输对象。
 */
@Data
@NoArgsConstructor
public class StoredObjectDTO {

    /** 主键。 */
    private StoredObjectId id;
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
    /** 对象状态。 */
    private String objectStatus;
    /** 引用状态。 */
    private String referenceStatus;
    /** 创建时间。 */
    private Instant createdAt;

    public StoredObjectDTO(
            StoredObjectId id,
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
        this.id = id;
        this.storageType = storageType;
        this.bucketName = bucketName;
        this.objectKey = objectKey;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.size = size;
        this.accessEndpoint = accessEndpoint;
        this.objectStatus = objectStatus;
        this.referenceStatus = referenceStatus;
        this.createdAt = createdAt;
    }
}
