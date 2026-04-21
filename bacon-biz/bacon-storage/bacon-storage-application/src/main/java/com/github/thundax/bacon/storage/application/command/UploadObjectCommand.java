package com.github.thundax.bacon.storage.application.command;

import java.io.InputStream;

/**
 * 上传存储对象命令。
 */
public record UploadObjectCommand(
        String ownerType,
        String category,
        String originalFilename,
        String contentType,
        Long size,
        InputStream inputStream) {}
