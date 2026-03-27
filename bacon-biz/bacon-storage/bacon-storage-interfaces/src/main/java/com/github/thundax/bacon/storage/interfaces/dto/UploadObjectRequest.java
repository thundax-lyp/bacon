package com.github.thundax.bacon.storage.interfaces.dto;

import org.springframework.web.multipart.MultipartFile;

/**
 * 上传存储对象请求。
 */
public record UploadObjectRequest(
        String ownerType,
        String tenantId,
        String category,
        MultipartFile file) {
}
