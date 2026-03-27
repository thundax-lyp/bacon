package com.github.thundax.bacon.storage.interfaces.response;

import com.github.thundax.bacon.storage.api.dto.StoredObjectDTO;

import java.time.Instant;

/**
 * 存储对象响应。
 */
public record StoredObjectResponse(
        /** 主键。 */
        Long id,
        /** 底层存储类型。 */
        String storageType,
        /** 存储桶或本地逻辑目录。 */
        String bucketName,
        /** 底层对象键。 */
        String objectKey,
        /** 原始文件名。 */
        String originalFilename,
        /** 内容类型。 */
        String contentType,
        /** 文件大小，字节。 */
        Long size,
        /** 由 Storage 派生的对象访问端点，仅用于展示/下载，不作为业务主数据持久化。 */
        String accessEndpoint,
        /** 已废弃，请改用 accessEndpoint。 */
        @Deprecated
        String accessUrl,
        /** 对象状态。 */
        String objectStatus,
        /** 引用状态。 */
        String referenceStatus,
        /** 创建时间。 */
        Instant createdAt) {

    public static StoredObjectResponse from(StoredObjectDTO dto) {
        return new StoredObjectResponse(dto.getId(), dto.getStorageType(), dto.getBucketName(), dto.getObjectKey(),
                dto.getOriginalFilename(), dto.getContentType(), dto.getSize(), dto.getAccessEndpoint(),
                dto.getAccessUrl(), dto.getObjectStatus(), dto.getReferenceStatus(), dto.getCreatedAt());
    }
}
