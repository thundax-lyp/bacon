package com.github.thundax.bacon.storage.application.assembler;

import com.github.thundax.bacon.storage.api.dto.MultipartUploadSessionDTO;
import com.github.thundax.bacon.storage.domain.model.entity.MultipartUploadSession;
import com.github.thundax.bacon.storage.domain.model.enums.UploadStatus;

public final class MultipartUploadSessionAssembler {

    private MultipartUploadSessionAssembler() {}

    public static MultipartUploadSessionDTO toDto(MultipartUploadSession multipartUploadSession) {
        if (multipartUploadSession == null) {
            return null;
        }
        return new MultipartUploadSessionDTO(
                multipartUploadSession.getUploadId(),
                multipartUploadSession.getOwnerType(),
                multipartUploadSession.getOwnerId(),
                multipartUploadSession.getCategory(),
                multipartUploadSession.getOriginalFilename(),
                multipartUploadSession.getContentType(),
                multipartUploadSession.getTotalSize(),
                multipartUploadSession.getPartSize(),
                multipartUploadSession.getUploadedPartCount(),
                multipartUploadSession.getUploadStatus() == null
                        ? null
                        : multipartUploadSession.getUploadStatus().value());
    }

    public static MultipartUploadSession toDomain(MultipartUploadSessionDTO multipartUploadSessionDTO) {
        if (multipartUploadSessionDTO == null) {
            return null;
        }
        return MultipartUploadSession.reconstruct(
                null,
                multipartUploadSessionDTO.getUploadId(),
                multipartUploadSessionDTO.getOwnerType(),
                multipartUploadSessionDTO.getOwnerId(),
                multipartUploadSessionDTO.getCategory(),
                multipartUploadSessionDTO.getOriginalFilename(),
                multipartUploadSessionDTO.getContentType(),
                null,
                null,
                multipartUploadSessionDTO.getTotalSize(),
                multipartUploadSessionDTO.getPartSize(),
                multipartUploadSessionDTO.getUploadedPartCount(),
                multipartUploadSessionDTO.getUploadStatus() == null
                        ? null
                        : UploadStatus.from(multipartUploadSessionDTO.getUploadStatus()),
                null,
                null,
                null,
                null);
    }
}
