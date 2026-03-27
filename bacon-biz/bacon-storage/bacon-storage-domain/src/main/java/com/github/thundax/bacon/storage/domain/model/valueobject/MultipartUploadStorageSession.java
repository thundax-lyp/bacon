package com.github.thundax.bacon.storage.domain.model.valueobject;

/**
 * 底层存储分段上传初始化结果。
 */
public record MultipartUploadStorageSession(
        /** Storage 统一生成的对象键。 */
        String objectKey,
        /** 底层存储提供方分段上传会话标识，本地文件模式下可为空。 */
        String providerUploadId) {
}
