package com.github.thundax.bacon.storage.interfaces.dto;

/**
 * 完成分段上传请求。
 */
public record CompleteMultipartUploadRequest(
        String ownerId) {
}
