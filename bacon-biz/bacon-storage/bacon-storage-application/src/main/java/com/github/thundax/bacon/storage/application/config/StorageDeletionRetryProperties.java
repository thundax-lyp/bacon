package com.github.thundax.bacon.storage.application.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Storage 删除补偿重试配置。
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "bacon.storage.deletion-retry")
public class StorageDeletionRetryProperties {

    /** 是否启用删除补偿。 */
    private boolean enabled = true;

    /** 调度固定间隔。 */
    private long fixedDelayMillis = 60_000L;

    /** 单次补偿批次大小。 */
    private int batchSize = 100;
}
