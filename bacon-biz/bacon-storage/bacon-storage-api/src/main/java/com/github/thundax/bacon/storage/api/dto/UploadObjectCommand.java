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

    private String ownerType;
    private String tenantId;
    private String category;
    private String originalFilename;
    private String contentType;
    private Long size;
    private InputStream inputStream;
}
