package com.github.thundax.bacon.storage.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.InputStream;

/**
 * 上传存储对象命令。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadObjectCommand {

    /** 引用方类型。 */
    private String ownerType;
    /** 所属租户业务键。 */
    private Long tenantId;
    /** 对象分类。 */
    private String category;
    /** 原始文件名。 */
    private String originalFilename;
    /** 内容类型。 */
    private String contentType;
    /** 文件大小，字节。 */
    private Long size;
    /** 上传输入流。 */
    private InputStream inputStream;
}
