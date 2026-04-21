package com.github.thundax.bacon.storage.application.command;

/**
 * 完成分段上传命令。
 */
public record CompleteMultipartUploadCommand(String uploadId, String ownerType, String ownerId) {}
