package com.github.thundax.bacon.storage.interfaces.response;

import com.github.thundax.bacon.storage.api.dto.MultipartUploadSessionDTO;

/**
 * 分段上传会话响应。
 */
public record MultipartUploadSessionResponse(
        /** 分段上传会话业务键。 */
        String uploadId,
        /** 引用方类型。 */
        String ownerType,
        /** 所属租户业务键。 */
        String tenantId,
        /** 对象分类。 */
        String category,
        /** 原始文件名。 */
        String originalFilename,
        /** 内容类型。 */
        String contentType,
        /** 总文件大小，字节。 */
        Long totalSize,
        /** 固定分段大小，字节。 */
        Long partSize,
        /** 已上传分段数。 */
        Integer uploadedPartCount,
        /** 分段上传状态。 */
        String uploadStatus) {

    public static MultipartUploadSessionResponse from(MultipartUploadSessionDTO dto) {
        return new MultipartUploadSessionResponse(dto.getUploadId(), dto.getOwnerType(), dto.getTenantId(),
                dto.getCategory(), dto.getOriginalFilename(), dto.getContentType(), dto.getTotalSize(),
                dto.getPartSize(), dto.getUploadedPartCount(), dto.getUploadStatus());
    }
}
