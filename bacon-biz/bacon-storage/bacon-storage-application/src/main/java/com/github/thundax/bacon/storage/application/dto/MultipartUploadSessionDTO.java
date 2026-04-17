package com.github.thundax.bacon.storage.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分段上传会话传输对象。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultipartUploadSessionDTO {

    /** 分段上传会话业务键。 */
    private String uploadId;
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
    /** 总文件大小，字节。 */
    private Long totalSize;
    /** 固定分段大小，字节。 */
    private Long partSize;
    /** 已上传分段数。 */
    private Integer uploadedPartCount;
    /** 分段上传状态。 */
    private String uploadStatus;
}
