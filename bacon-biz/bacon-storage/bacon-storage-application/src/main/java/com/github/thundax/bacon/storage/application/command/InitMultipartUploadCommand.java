package com.github.thundax.bacon.storage.application.command;

/**
 * 初始化分段上传命令。
 */
public record InitMultipartUploadCommand(
        String ownerType,
        String ownerId,
        String category,
        String originalFilename,
        String contentType,
        Long totalSize,
        Long partSize) {}
