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
    /** 由 Storage 派生的对象访问端点，仅用于展示/下载，不作为业务主数据持久化。 */
    private final String accessEndpoint;

    /** 兼容旧命名，后续请使用 getAccessEndpoint。 */
    @Deprecated
    public String getAccessUrl() {
        return accessEndpoint;
    }
}
