package com.github.thundax.bacon.common.mq.support;

import com.github.thundax.bacon.common.mq.BaconMqMessage;
import com.github.thundax.bacon.common.mq.BaconMqSender;
import org.springframework.kafka.core.KafkaTemplate;

public class KafkaBaconMqSender implements BaconMqSender {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaBaconMqSender(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void send(BaconMqMessage message) {
        kafkaTemplate.send(message.getTopic(), message.getKey(), message.getPayload());
    }
}
