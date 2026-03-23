package com.github.thundax.bacon.common.mq.config;

import com.github.thundax.bacon.common.mq.BaconMqSender;
import com.github.thundax.bacon.common.mq.support.KafkaBaconMqSender;
import com.github.thundax.bacon.common.mq.support.NoOpBaconMqSender;
import com.github.thundax.bacon.common.mq.support.RabbitBaconMqSender;
import com.github.thundax.bacon.common.mq.support.RocketMqBaconMqSender;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration(proxyBeanMethods = false)
public class BaconMqSenderConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "bacon.mq", name = "enabled", havingValue = "false")
    @ConditionalOnMissingBean(BaconMqSender.class)
    public BaconMqSender disabledBaconMqSender() {
        return new NoOpBaconMqSender();
    }

    @Bean
    @ConditionalOnClass(RocketMQTemplate.class)
    @ConditionalOnProperty(prefix = "bacon.mq", name = "type", havingValue = "ROCKETMQ", matchIfMissing = true)
    @ConditionalOnMissingBean(BaconMqSender.class)
    public BaconMqSender rocketMqSender(RocketMQTemplate rocketMQTemplate) {
        return new RocketMqBaconMqSender(rocketMQTemplate);
    }

    @Bean
    @ConditionalOnClass(RabbitTemplate.class)
    @ConditionalOnProperty(prefix = "bacon.mq", name = "type", havingValue = "RABBITMQ")
    @ConditionalOnMissingBean(BaconMqSender.class)
    public BaconMqSender rabbitMqSender(RabbitTemplate rabbitTemplate) {
        return new RabbitBaconMqSender(rabbitTemplate);
    }

    @Bean
    @ConditionalOnClass(KafkaTemplate.class)
    @ConditionalOnProperty(prefix = "bacon.mq", name = "type", havingValue = "KAFKA")
    @ConditionalOnMissingBean(BaconMqSender.class)
    public BaconMqSender kafkaMqSender(KafkaTemplate<String, Object> kafkaTemplate) {
        return new KafkaBaconMqSender(kafkaTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(BaconMqSender.class)
    public BaconMqSender fallbackBaconMqSender() {
        return new NoOpBaconMqSender();
    }
}
