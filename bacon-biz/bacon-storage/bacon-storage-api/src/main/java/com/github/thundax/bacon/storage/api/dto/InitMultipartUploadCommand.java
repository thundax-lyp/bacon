package com.github.thundax.bacon.storage.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 初始化分段上传命令。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InitMultipartUploadCommand {

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
}
