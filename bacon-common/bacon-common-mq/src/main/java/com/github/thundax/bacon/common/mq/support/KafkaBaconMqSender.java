package com.github.thundax.bacon.common.mq.support;

import com.github.thundax.bacon.common.mq.BaconMqMessage;
import com.github.thundax.bacon.common.mq.BaconMqSender;
import java.util.Map;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.support.MessageBuilder;

public class KafkaBaconMqSender implements BaconMqSender {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaBaconMqSender(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void send(BaconMqMessage message) {
        MessageBuilder<Object> builder = MessageBuilder.withPayload(message.getPayload())
                .setHeader(KafkaHeaders.TOPIC, message.getTopic());
        if (message.getKey() != null) {
            builder.setHeader(KafkaHeaders.KEY, message.getKey());
        }
        for (Map.Entry<String, String> entry : BaconMqHeaderSupport.resolveHeaders(message).entrySet()) {
            builder.setHeader(entry.getKey(), entry.getValue());
        }
        kafkaTemplate.send(builder.build());
    }
}
