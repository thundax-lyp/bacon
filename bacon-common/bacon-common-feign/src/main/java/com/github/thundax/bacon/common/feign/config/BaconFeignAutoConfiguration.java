package com.github.thundax.bacon.common.feign.config;

import com.github.thundax.bacon.common.feign.properties.BaconFeignProperties;
import feign.Logger;
import feign.Retryer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass({feign.Feign.class, Logger.Level.class, Retryer.class})
@EnableConfigurationProperties(BaconFeignProperties.class)
public class BaconFeignAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(Logger.Level.class)
    public Logger.Level baconFeignLoggerLevel(BaconFeignProperties properties) {
        return Logger.Level.valueOf(properties.getLoggerLevel().trim().toUpperCase());
    }

    @Bean
    @ConditionalOnMissingBean(Retryer.class)
    public Retryer baconFeignRetryer(BaconFeignProperties properties) {
        return properties.isRetryEnabled() ? new Retryer.Default() : Retryer.NEVER_RETRY;
    }
}
