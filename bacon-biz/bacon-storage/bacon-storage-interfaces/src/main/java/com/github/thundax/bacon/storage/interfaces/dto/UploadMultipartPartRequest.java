package com.github.thundax.bacon.storage.interfaces.dto;

import org.springframework.web.multipart.MultipartFile;

/**
 * 上传分段请求。
 */
public record UploadMultipartPartRequest(
        String ownerType,
        String ownerId,
        String tenantId,
        Integer partNumber,
        MultipartFile file) {
}
