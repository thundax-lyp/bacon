package com.github.thundax.bacon.storage.interfaces.dto;

/**
 * 初始化分段上传请求。
 */
public record InitMultipartUploadRequest(
        String ownerType,
        String ownerId,
        String tenantId,
        String category,
        String originalFilename,
        String contentType,
        Long totalSize,
        Long partSize) {
}
