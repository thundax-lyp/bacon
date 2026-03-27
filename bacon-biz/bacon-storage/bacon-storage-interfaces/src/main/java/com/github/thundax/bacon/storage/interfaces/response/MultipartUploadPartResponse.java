package com.github.thundax.bacon.storage.interfaces.response;

import com.github.thundax.bacon.storage.api.dto.MultipartUploadPartDTO;

/**
 * 分段上传分片响应。
 */
public record MultipartUploadPartResponse(
        /** 分段上传会话业务键。 */
        String uploadId,
        /** 分段序号。 */
        Integer partNumber,
        /** 分段校验标识。 */
        String etag) {

    public static MultipartUploadPartResponse from(MultipartUploadPartDTO dto) {
        return new MultipartUploadPartResponse(dto.getUploadId(), dto.getPartNumber(), dto.getEtag());
    }
}
