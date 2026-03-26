package com.github.thundax.bacon.upms.interfaces.consumer;

import com.github.thundax.bacon.common.log.dto.SysLogDTO;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "bacon.mq", name = "type", havingValue = "RABBITMQ")
public class RabbitSysLogConsumer {

    private final SysLogMqConsumer sysLogMqConsumer;

    public RabbitSysLogConsumer(SysLogMqConsumer sysLogMqConsumer) {
        this.sysLogMqConsumer = sysLogMqConsumer;
    }

    @RabbitListener(queues = "${bacon.log.sys.queue:bacon.sys.log.queue}")
    public void onMessage(SysLogDTO sysLogDTO) {
        sysLogMqConsumer.consume(sysLogDTO);
    }
}
