package com.github.thundax.bacon.storage.api.enums;

/**
 * 分段上传状态。
 */
public enum UploadStatusEnum {

    INITIATED,
    UPLOADING,
    COMPLETED,
    ABORTED;

    public static UploadStatusEnum from(String value) {
        return value == null ? null : UploadStatusEnum.valueOf(value);
    }
}
