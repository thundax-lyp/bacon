package com.github.thundax.bacon.storage.infra.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Storage 远程客户端配置。
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "bacon.remote.storage-client")
public class StorageRemoteClientProperties {

    /** 连接超时。 */
    private Duration connectTimeout = Duration.ofSeconds(3);

    /** 读取超时。 */
    private Duration readTimeout = Duration.ofSeconds(30);
}
