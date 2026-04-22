package com.github.thundax.bacon.storage.interfaces.response;

import com.github.thundax.bacon.storage.application.dto.MultipartUploadPartDTO;

public record MultipartUploadPartResponse(String uploadId, Integer partNumber, String etag) {

    public static MultipartUploadPartResponse from(MultipartUploadPartDTO dto) {
        return new MultipartUploadPartResponse(dto.getUploadId(), dto.getPartNumber(), dto.getEtag());
    }
}
