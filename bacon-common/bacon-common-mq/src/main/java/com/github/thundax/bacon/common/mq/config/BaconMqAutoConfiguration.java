package com.github.thundax.bacon.common.mq.config;

import com.github.thundax.bacon.common.mq.BaconMqProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(BaconMqProperties.class)
public class BaconMqAutoConfiguration {
}
