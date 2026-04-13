package com.github.thundax.bacon.storage.application.assembler;

import com.github.thundax.bacon.storage.api.dto.MultipartUploadPartDTO;
import com.github.thundax.bacon.storage.domain.model.entity.MultipartUploadPart;

public final class MultipartUploadPartAssembler {

    private MultipartUploadPartAssembler() {}

    public static MultipartUploadPartDTO toDto(MultipartUploadPart multipartUploadPart) {
        if (multipartUploadPart == null) {
            return null;
        }
        return new MultipartUploadPartDTO(
                multipartUploadPart.getUploadId(),
                multipartUploadPart.getPartNumber(),
                multipartUploadPart.getEtag());
    }

    public static MultipartUploadPart toDomain(MultipartUploadPartDTO multipartUploadPartDTO) {
        if (multipartUploadPartDTO == null) {
            return null;
        }
        return MultipartUploadPart.reconstruct(
                null,
                multipartUploadPartDTO.getUploadId(),
                multipartUploadPartDTO.getPartNumber(),
                multipartUploadPartDTO.getEtag(),
                null,
                null);
    }
}
