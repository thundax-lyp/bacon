package com.github.thundax.bacon.common.mq.support;

import com.github.thundax.bacon.common.mq.BaconMqMessage;
import com.github.thundax.bacon.common.mq.BaconMqSender;
import org.apache.rocketmq.spring.core.RocketMQTemplate;

public class RocketMqBaconMqSender implements BaconMqSender {

    private final RocketMQTemplate rocketMQTemplate;

    public RocketMqBaconMqSender(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    @Override
    public void send(BaconMqMessage message) {
        rocketMQTemplate.syncSend(buildDestination(message), message.getPayload());
    }

    private String buildDestination(BaconMqMessage message) {
        if (message.getTag() == null || message.getTag().isBlank()) {
            return message.getTopic();
        }
        return message.getTopic() + ":" + message.getTag();
    }
}
