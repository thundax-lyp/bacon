package com.github.thundax.bacon.upms.infra.mq;

import com.github.thundax.bacon.common.log.dto.SysLogDTO;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "bacon.mq", name = "type", havingValue = "ROCKETMQ", matchIfMissing = true)
@RocketMQMessageListener(
        topic = "${bacon.log.sys.topic:bacon-sys-log}",
        consumerGroup = "${bacon.log.sys.consumer-group:bacon-upms-sys-log-group}",
        selectorExpression = "${bacon.log.sys.tag:sys-log}",
        messageModel = MessageModel.CLUSTERING,
        consumeMode = ConsumeMode.ORDERLY
)
public class RocketMqSysLogConsumer implements RocketMQListener<SysLogDTO> {

    private final SysLogMqConsumer sysLogMqConsumer;

    public RocketMqSysLogConsumer(SysLogMqConsumer sysLogMqConsumer) {
        this.sysLogMqConsumer = sysLogMqConsumer;
    }

    @Override
    public void onMessage(SysLogDTO sysLogDTO) {
        sysLogMqConsumer.consume(sysLogDTO);
    }
}
