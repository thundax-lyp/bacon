package com.github.thundax.bacon.storage.domain.model.valueobject;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 底层存储写入结果。
 */
@Getter
@AllArgsConstructor
public class StoredObjectStorageResult {

    /** 底层存储类型。 */
    private final String storageType;
    /** 存储桶或本地逻辑目录。 */
    private final String bucketName;
    /** 底层对象键。 */
    private final String objectKey;
    /** 当前访问地址。 */
    private final String accessUrl;
}
