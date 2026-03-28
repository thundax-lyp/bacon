package com.github.thundax.bacon.storage.domain.model.valueobject;

/**
 * 存储对象分页查询条件。
 */
public record StoredObjectPageQuery(
        String tenantId,
        String storageType,
        String objectStatus,
        String referenceStatus,
        String originalFilename,
        String objectKey,
        int offset,
        int limit
) {
}
