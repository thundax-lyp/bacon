package com.github.thundax.bacon.upms.interfaces.consumer;

import com.github.thundax.bacon.common.log.dto.SysLogDTO;
import com.github.thundax.bacon.common.mq.BaconMqHeaders;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "bacon.mq", name = "type", havingValue = "RABBITMQ")
public class RabbitSysLogConsumer {

    private final SysLogMqConsumer sysLogMqConsumer;

    public RabbitSysLogConsumer(SysLogMqConsumer sysLogMqConsumer) {
        this.sysLogMqConsumer = sysLogMqConsumer;
    }

    @RabbitListener(queues = "${bacon.log.sys.queue:bacon.sys.log.queue}")
    public void onMessage(
            @Payload SysLogDTO sysLogDTO, @Header(name = BaconMqHeaders.TENANT_ID, required = false) Long tenantId) {
        sysLogMqConsumer.consume(tenantId, sysLogDTO);
    }
}
