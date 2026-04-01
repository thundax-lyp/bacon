package com.github.thundax.bacon.common.log.config;

import com.github.thundax.bacon.common.log.aspect.SysLogAspect;
import com.github.thundax.bacon.common.log.producer.BaconMqSysLogProperties;
import com.github.thundax.bacon.common.log.producer.MqSysLogMessageProducer;
import com.github.thundax.bacon.common.log.producer.NoOpSysLogMessageProducer;
import com.github.thundax.bacon.common.log.producer.SysLogMessageProducer;
import com.github.thundax.bacon.common.mq.BaconMqProperties;
import com.github.thundax.bacon.common.mq.BaconMqSender;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@AutoConfiguration
@EnableConfigurationProperties(BaconMqSysLogProperties.class)
public class BaconLogAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SysLogMessageProducer noOpSysLogMessageProducer() {
        return new NoOpSysLogMessageProducer();
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(MqSysLogMessageProducer.class)
    public SysLogMessageProducer mqSysLogMessageProducer(BaconMqSender baconMqSender,
                                                         BaconMqProperties baconMqProperties,
                                                         BaconMqSysLogProperties baconMqSysLogProperties) {
        return new MqSysLogMessageProducer(baconMqSender, baconMqProperties, baconMqSysLogProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public SysLogAspect sysLogAspect(SysLogMessageProducer sysLogMessageProducer) {
        return new SysLogAspect(sysLogMessageProducer);
    }
}
