package com.github.thundax.bacon.storage.domain.model.valueobject;

import com.github.thundax.bacon.storage.domain.model.enums.StorageType;

/**
 * 底层存储写入结果。
 */
public record StoredObjectStorageResult(
        /** 底层存储类型。 */
        StorageType storageType,
        /** 存储桶或本地逻辑目录。 */
        String bucketName,
        /** 底层对象键。 */
        String objectKey,
        /** 由 Storage 派生的对象访问端点，仅用于展示/下载，不作为业务主数据持久化。 */
        String accessEndpoint) {}
