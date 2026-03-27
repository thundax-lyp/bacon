package com.github.thundax.bacon.common.oss.config;

import com.github.thundax.bacon.common.oss.client.ObjectStorageClient;
import com.github.thundax.bacon.common.oss.client.S3ApiObjectStorageClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * 通用对象存储自动配置。
 */
@AutoConfiguration
@ConditionalOnClass(S3Client.class)
@EnableConfigurationProperties(CommonOssProperties.class)
public class CommonOssAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "bacon.common.oss", name = "enabled", havingValue = "true")
    public ObjectStorageClient objectStorageClient(CommonOssProperties properties) {
        return new S3ApiObjectStorageClient(properties);
    }
}
