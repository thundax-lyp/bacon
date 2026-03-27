package com.github.thundax.bacon.storage.domain.model.entity;

import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * 分段上传会话实体。
 */
@Getter
public class MultipartUploadSession {

    public static final String STATUS_INITIATED = "INITIATED";
    public static final String STATUS_UPLOADING = "UPLOADING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_ABORTED = "ABORTED";

    /** 主键。 */
    private Long id;
    /** 分段上传会话业务键。 */
    private String uploadId;
    /** 所属租户业务键。 */
    private String tenantId;
    /** 引用方类型。 */
    private String ownerType;
    /** 引用方业务主键。 */
    private String ownerId;
    /** 对象分类。 */
    private String category;
    /** 原始文件名。 */
    private String originalFilename;
    /** 内容类型。 */
    private String contentType;
    /** Storage 统一生成的对象键。 */
    private String objectKey;
    /** 底层存储提供方分段上传会话标识。 */
    private String providerUploadId;
    /** 总文件大小，字节。 */
    private Long totalSize;
    /** 固定分段大小，字节。 */
    private Long partSize;
    /** 已上传分段数。 */
    private Integer uploadedPartCount;
    /** 分段上传状态。 */
    private String uploadStatus;
    /** 创建时间。 */
    private Instant createdAt;
    /** 更新时间。 */
    private Instant updatedAt;
    /** 完成时间。 */
    private Instant completedAt;
    /** 取消时间。 */
    private Instant abortedAt;

    public MultipartUploadSession(Long id, String uploadId, String tenantId, String ownerType, String ownerId,
                                  String category, String originalFilename, String contentType, String objectKey,
                                  String providerUploadId, Long totalSize, Long partSize, Integer uploadedPartCount,
                                  String uploadStatus, Instant createdAt, Instant updatedAt, Instant completedAt,
                                  Instant abortedAt) {
        this.id = id;
        this.uploadId = uploadId;
        this.tenantId = tenantId;
        this.ownerType = ownerType;
        this.ownerId = ownerId;
        this.category = category;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.objectKey = objectKey;
        this.providerUploadId = providerUploadId;
        this.totalSize = totalSize;
        this.partSize = partSize;
        this.uploadedPartCount = uploadedPartCount;
        this.uploadStatus = uploadStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.completedAt = completedAt;
        this.abortedAt = abortedAt;
    }

    public static MultipartUploadSession initiate(String uploadId, String tenantId, String ownerType, String ownerId,
                                                  String category, String originalFilename, String contentType,
                                                  String objectKey, String providerUploadId, Long totalSize,
                                                  Long partSize) {
        requireText(uploadId, "uploadId");
        requireText(ownerType, "ownerType");
        requireText(ownerId, "ownerId");
        requireText(objectKey, "objectKey");
        requirePositive(totalSize, "totalSize");
        requirePositive(partSize, "partSize");
        Instant now = Instant.now();
        return new MultipartUploadSession(null, uploadId, tenantId, ownerType, ownerId, category, originalFilename,
                contentType, objectKey, providerUploadId, totalSize, partSize, 0, STATUS_INITIATED, now, now,
                null, null);
    }

    public boolean isCompleted() {
        return STATUS_COMPLETED.equals(this.uploadStatus);
    }

    public boolean isAborted() {
        return STATUS_ABORTED.equals(this.uploadStatus);
    }

    public void recordUploadedPart() {
        ensureAcceptingUpload();
        this.uploadedPartCount = Objects.requireNonNullElse(this.uploadedPartCount, 0) + 1;
        this.uploadStatus = STATUS_UPLOADING;
        this.updatedAt = Instant.now();
    }

    public void markCompleted() {
        ensureAcceptingUpload();
        Instant now = Instant.now();
        this.uploadStatus = STATUS_COMPLETED;
        this.updatedAt = now;
        this.completedAt = now;
    }

    public void markAborted() {
        if (isCompleted()) {
            throw new IllegalStateException("Completed upload session cannot be aborted");
        }
        Instant now = Instant.now();
        this.uploadStatus = STATUS_ABORTED;
        this.updatedAt = now;
        this.abortedAt = now;
    }

    public void assertOwnership(String tenantId, String ownerType, String ownerId) {
        if (!Objects.equals(this.tenantId, tenantId)) {
            throw new IllegalArgumentException("Multipart upload session tenantId mismatch");
        }
        if (!Objects.equals(this.ownerType, ownerType)) {
            throw new IllegalArgumentException("Multipart upload session ownerType mismatch");
        }
        if (!Objects.equals(this.ownerId, ownerId)) {
            throw new IllegalArgumentException("Multipart upload session ownerId mismatch");
        }
    }

    public void assertCompletable(List<MultipartUploadPart> parts) {
        ensureAcceptingUpload();
        if (parts == null || parts.isEmpty()) {
            throw new IllegalArgumentException("Multipart upload parts must not be empty");
        }
        int expectedPartCount = Math.toIntExact((totalSize + partSize - 1L) / partSize);
        if (parts.size() != expectedPartCount) {
            throw new IllegalArgumentException("Multipart upload part count mismatch");
        }
        if (!Objects.equals(uploadedPartCount, parts.size())) {
            throw new IllegalArgumentException("Multipart upload uploadedPartCount mismatch");
        }
        long totalUploadedSize = 0L;
        for (int index = 0; index < parts.size(); index++) {
            MultipartUploadPart part = parts.get(index);
            int expectedPartNumber = index + 1;
            if (!Objects.equals(part.getPartNumber(), expectedPartNumber)) {
                throw new IllegalArgumentException("Multipart upload part number is not continuous");
            }
            if (index < parts.size() - 1 && !Objects.equals(part.getSize(), partSize)) {
                throw new IllegalArgumentException("Multipart upload non-last part size mismatch");
            }
            if (index == parts.size() - 1 && (part.getSize() == null || part.getSize() <= 0L || part.getSize() > partSize)) {
                throw new IllegalArgumentException("Multipart upload last part size invalid");
            }
            totalUploadedSize += part.getSize();
        }
        if (!Objects.equals(totalUploadedSize, totalSize)) {
            throw new IllegalArgumentException("Multipart upload total size mismatch");
        }
    }

    private void ensureAcceptingUpload() {
        if (isCompleted()) {
            throw new IllegalStateException("Upload session is already completed");
        }
        if (isAborted()) {
            throw new IllegalStateException("Upload session is already aborted");
        }
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }

    private static void requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0L) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
    }
}
