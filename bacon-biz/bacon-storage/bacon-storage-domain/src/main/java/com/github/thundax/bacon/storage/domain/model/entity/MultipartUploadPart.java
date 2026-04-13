package com.github.thundax.bacon.storage.domain.model.entity;

import java.time.Instant;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 分段上传分段实体。
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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

    public static MultipartUploadPart create(
            Long id, String uploadId, Integer partNumber, String etag, Long size, Instant createdAt) {
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
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        return new MultipartUploadPart(id, uploadId, partNumber, etag, size, createdAt);
    }

    public static MultipartUploadPart reconstruct(
            Long id, String uploadId, Integer partNumber, String etag, Long size, Instant createdAt) {
        return new MultipartUploadPart(id, uploadId, partNumber, etag, size, createdAt);
    }
}
