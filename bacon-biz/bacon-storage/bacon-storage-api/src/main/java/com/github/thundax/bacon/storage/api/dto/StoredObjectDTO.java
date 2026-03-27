package com.github.thundax.bacon.storage.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * 存储对象传输对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoredObjectDTO {

    private Long id;
    private String storageType;
    private String bucketName;
    private String objectKey;
    private String originalFilename;
    private String contentType;
    private Long size;
    private String accessUrl;
    private String objectStatus;
    private String referenceStatus;
    private Instant createdAt;
}
