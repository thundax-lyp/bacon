package com.github.thundax.bacon.storage.domain.model.valueobject;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 底层存储写入结果。
 */
@Getter
@AllArgsConstructor
public class StoredObjectStorageResult {

    private final String storageType;
    private final String bucketName;
    private final String objectKey;
    private final String accessUrl;
}
