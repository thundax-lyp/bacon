package com.github.thundax.bacon.storage.application.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Storage 上传限制配置。
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "bacon.storage.upload-limit")
public class StorageUploadLimitProperties {

    /** 单次普通上传最大文件大小，默认 20MB。 */
    private long singleUploadMaxSizeBytes = 20L * 1024 * 1024;

    /** 分片上传固定单片大小，默认 8MB。 */
    private long multipartPartSizeBytes = 8L * 1024 * 1024;

    /** 分片上传总文件最大大小，默认 4GB。 */
    private long multipartMaxTotalSizeBytes = 4L * 1024 * 1024 * 1024;
}
