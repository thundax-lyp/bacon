package com.github.thundax.bacon.common.mq.support;

import com.github.thundax.bacon.common.mq.BaconMqMessage;
import com.github.thundax.bacon.common.mq.BaconMqSender;
import java.util.Map;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.messaging.support.MessageBuilder;

public class RocketMqBaconMqSender implements BaconMqSender {

    private final RocketMQTemplate rocketMQTemplate;

    public RocketMqBaconMqSender(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    @Override
    public void send(BaconMqMessage message) {
        MessageBuilder<Object> builder = MessageBuilder.withPayload(message.getPayload());
        if (message.getKey() != null) {
            builder.setHeader(RocketMQHeaders.KEYS, message.getKey());
        }
        if (message.getTag() != null) {
            builder.setHeader(RocketMQHeaders.TAGS, message.getTag());
        }
        for (Map.Entry<String, String> entry :
                BaconMqHeaderSupport.resolveHeaders(message).entrySet()) {
            builder.setHeader(entry.getKey(), entry.getValue());
        }
        rocketMQTemplate.syncSend(buildDestination(message), builder.build());
    }

    private String buildDestination(BaconMqMessage message) {
        if (message.getTag() == null || message.getTag().isBlank()) {
            return message.getTopic();
        }
        return message.getTopic() + ":" + message.getTag();
    }
}
