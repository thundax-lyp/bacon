package com.github.thundax.bacon.storage.interfaces.response;

import com.github.thundax.bacon.storage.application.dto.MultipartUploadSessionDTO;

public record MultipartUploadSessionResponse(
        String uploadId,
        String ownerType,
        String ownerId,
        String category,
        String originalFilename,
        String contentType,
        Long totalSize,
        Long partSize,
        Integer uploadedPartCount,
        String uploadStatus) {

    public static MultipartUploadSessionResponse from(MultipartUploadSessionDTO dto) {
        return new MultipartUploadSessionResponse(
                dto.getUploadId(),
                dto.getOwnerType(),
                dto.getOwnerId(),
                dto.getCategory(),
                dto.getOriginalFilename(),
                dto.getContentType(),
                dto.getTotalSize(),
                dto.getPartSize(),
                dto.getUploadedPartCount(),
                dto.getUploadStatus());
    }
}
