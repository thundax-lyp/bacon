package com.github.thundax.bacon.common.oss.model;

/**
 * 对象存储写入结果。
 */
public record ObjectStorageWriteResult(
        /** 桶名称。 */
        String bucketName,
        /** 对象键。 */
        String objectKey,
        /** 访问端点。 */
        String accessEndpoint) {
}
