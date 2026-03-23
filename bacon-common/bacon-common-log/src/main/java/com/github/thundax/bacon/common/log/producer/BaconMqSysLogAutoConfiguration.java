package com.github.thundax.bacon.common.log.producer;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(BaconMqSysLogProperties.class)
public class BaconMqSysLogAutoConfiguration {
}
