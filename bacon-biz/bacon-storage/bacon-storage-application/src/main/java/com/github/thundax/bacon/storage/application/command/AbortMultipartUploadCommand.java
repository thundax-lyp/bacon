package com.github.thundax.bacon.storage.application.command;

/**
 * 取消分段上传命令。
 */
public record AbortMultipartUploadCommand(String uploadId, String ownerType, String ownerId) {}
