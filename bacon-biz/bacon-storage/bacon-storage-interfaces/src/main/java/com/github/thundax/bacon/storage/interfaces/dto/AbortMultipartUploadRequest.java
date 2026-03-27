package com.github.thundax.bacon.storage.interfaces.dto;

/**
 * 取消分段上传请求。
 */
public record AbortMultipartUploadRequest(
        String ownerType,
        String ownerId,
        String tenantId) {
}
