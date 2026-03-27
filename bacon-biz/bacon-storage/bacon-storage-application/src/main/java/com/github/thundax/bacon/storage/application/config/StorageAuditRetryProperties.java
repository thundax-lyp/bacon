package com.github.thundax.bacon.storage.application.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Storage 审计补偿重试配置。
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "bacon.storage.audit-retry")
public class StorageAuditRetryProperties {

    /** 是否启用审计补偿重试。 */
    private boolean enabled = true;

    /** 固定调度间隔。 */
    private long fixedDelayMillis = 10_000L;

    /** 单次重试批次大小。 */
    private int batchSize = 100;

    /** 最大重试次数。 */
    private int maxRetries = 6;

    /** 基础退避秒数。 */
    private long baseDelaySeconds = 30L;

    /** 最大退避秒数。 */
    private long maxDelaySeconds = 1800L;

    /** DEAD outbox 保留时长。 */
    private long deadRetentionSeconds = 7L * 24 * 60 * 60;

    /** 单次清理批次大小。 */
    private int cleanupBatchSize = 100;
}
