package com.github.thundax.bacon.storage.application.command;

import java.io.InputStream;

/**
 * 上传分段命令。
 */
public record UploadMultipartPartCommand(
        String uploadId,
        String ownerType,
        String ownerId,
        Integer partNumber,
        Long size,
        InputStream inputStream) {}
