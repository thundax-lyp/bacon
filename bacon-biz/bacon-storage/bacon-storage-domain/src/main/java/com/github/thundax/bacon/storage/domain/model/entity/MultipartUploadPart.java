package com.github.thundax.bacon.storage.domain.model.entity;

import lombok.Getter;

import java.time.Instant;

/**
 * 分段上传分段实体。
 */
@Getter
public class MultipartUploadPart {

    /** 主键。 */
    private Long id;
    /** 分段上传会话业务键。 */
    private String uploadId;
    /** 分段序号。 */
    private Integer partNumber;
    /** 分段校验标识。 */
    private String etag;
    /** 分段大小，字节。 */
    private Long size;
    /** 创建时间。 */
    private Instant createdAt;

    public MultipartUploadPart(Long id, String uploadId, Integer partNumber, String etag, Long size, Instant createdAt) {
        this.id = id;
        this.uploadId = uploadId;
        this.partNumber = partNumber;
        this.etag = etag;
        this.size = size;
        this.createdAt = createdAt;
    }

    public static MultipartUploadPart create(String uploadId, Integer partNumber, String etag, Long size) {
        if (uploadId == null || uploadId.isBlank()) {
            throw new IllegalArgumentException("uploadId must not be blank");
        }
        if (partNumber == null || partNumber <= 0) {
            throw new IllegalArgumentException("partNumber must be positive");
        }
        if (etag == null || etag.isBlank()) {
            throw new IllegalArgumentException("etag must not be blank");
        }
        if (size == null || size <= 0L) {
            throw new IllegalArgumentException("size must be positive");
        }
        return new MultipartUploadPart(null, uploadId, partNumber, etag, size, Instant.now());
    }
}
