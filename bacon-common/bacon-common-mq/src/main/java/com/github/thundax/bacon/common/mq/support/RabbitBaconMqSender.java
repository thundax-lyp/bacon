package com.github.thundax.bacon.common.mq.support;

import com.github.thundax.bacon.common.mq.BaconMqMessage;
import com.github.thundax.bacon.common.mq.BaconMqSender;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

public class RabbitBaconMqSender implements BaconMqSender {

    private final RabbitTemplate rabbitTemplate;

    public RabbitBaconMqSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void send(BaconMqMessage message) {
        rabbitTemplate.convertAndSend(message.getExchange(), message.getRoutingKey(), message.getPayload());
    }
}
