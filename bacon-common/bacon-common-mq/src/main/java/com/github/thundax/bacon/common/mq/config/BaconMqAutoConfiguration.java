package com.github.thundax.bacon.common.mq.config;

import com.github.thundax.bacon.common.mq.BaconMqProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@EnableConfigurationProperties(BaconMqProperties.class)
@Import(BaconMqSenderConfiguration.class)
public class BaconMqAutoConfiguration {
}
