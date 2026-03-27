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

    /** 主键。 */
    private Long id;
    /** 底层存储类型。 */
    private String storageType;
    /** 存储桶或本地逻辑目录。 */
    private String bucketName;
    /** 底层对象键。 */
    private String objectKey;
    /** 原始文件名。 */
    private String originalFilename;
    /** 内容类型。 */
    private String contentType;
    /** 文件大小，字节。 */
    private Long size;
    /** 当前访问地址。 */
    private String accessUrl;
    /** 对象状态。 */
    private String objectStatus;
    /** 引用状态。 */
    private String referenceStatus;
    /** 创建时间。 */
    private Instant createdAt;
}
