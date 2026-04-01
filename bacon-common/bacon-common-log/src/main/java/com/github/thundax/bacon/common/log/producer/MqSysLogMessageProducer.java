package com.github.thundax.bacon.common.log.producer;

import com.github.thundax.bacon.common.log.dto.SysLogDTO;
import com.github.thundax.bacon.common.mq.BaconMqMessage;
import com.github.thundax.bacon.common.mq.BaconMqProperties;
import com.github.thundax.bacon.common.mq.BaconMqSender;
import com.github.thundax.bacon.common.mq.BaconMqType;
import org.springframework.context.annotation.Primary;

@Primary
public class MqSysLogMessageProducer implements SysLogMessageProducer {

    private final BaconMqSender baconMqSender;
    private final BaconMqProperties baconMqProperties;
    private final BaconMqSysLogProperties baconMqSysLogProperties;

    public MqSysLogMessageProducer(BaconMqSender baconMqSender, BaconMqProperties baconMqProperties,
                                   BaconMqSysLogProperties baconMqSysLogProperties) {
        this.baconMqSender = baconMqSender;
        this.baconMqProperties = baconMqProperties;
        this.baconMqSysLogProperties = baconMqSysLogProperties;
    }

    @Override
    public void send(SysLogDTO message) {
        baconMqSender.send(buildMessage(message));
    }

    private BaconMqMessage buildMessage(SysLogDTO message) {
        if (baconMqProperties.getType() == BaconMqType.RABBITMQ) {
            return BaconMqMessage.forExchange(
                    baconMqSysLogProperties.getExchange(),
                    baconMqSysLogProperties.getRoutingKey(),
                    message.getTraceId(),
                    message
            );
        }
        if (baconMqProperties.getType() == BaconMqType.KAFKA) {
            return BaconMqMessage.forTopic(
                    baconMqSysLogProperties.getTopic(),
                    message.getTraceId(),
                    message
            );
        }
        return BaconMqMessage.forTopicWithTag(
                baconMqSysLogProperties.getTopic(),
                baconMqSysLogProperties.getTag(),
                message.getTraceId(),
                message
        );
    }
}
