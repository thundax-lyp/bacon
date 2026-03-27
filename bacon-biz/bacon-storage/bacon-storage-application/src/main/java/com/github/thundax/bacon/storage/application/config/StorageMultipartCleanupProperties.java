package com.github.thundax.bacon.storage.application.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Storage 分片上传清理配置。
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "bacon.storage.multipart-cleanup")
public class StorageMultipartCleanupProperties {

    /** 是否启用分片上传超时清理。 */
    private boolean enabled = true;

    /** 调度固定间隔，默认 10 分钟。 */
    private long fixedDelayMillis = 10L * 60 * 1000;

    /** 分片上传超时时间，默认 1 小时。 */
    private long timeoutSeconds = 60L * 60;

    /** 单次清理批次大小。 */
    private int batchSize = 100;
}
