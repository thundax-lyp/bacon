package com.github.thundax.bacon.common.mq.support;

import com.github.thundax.bacon.common.mq.BaconMqMessage;
import com.github.thundax.bacon.common.mq.BaconMqSender;
import java.util.Map;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

public class RabbitBaconMqSender implements BaconMqSender {

    private final RabbitTemplate rabbitTemplate;

    public RabbitBaconMqSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void send(BaconMqMessage message) {
        rabbitTemplate.convertAndSend(
                message.getExchange(), message.getRoutingKey(), message.getPayload(), mqMessage -> {
                    for (Map.Entry<String, String> entry :
                            BaconMqHeaderSupport.resolveHeaders(message).entrySet()) {
                        mqMessage.getMessageProperties().setHeader(entry.getKey(), entry.getValue());
                    }
                    return mqMessage;
                });
    }
}
