package com.github.thundax.bacon.upms.interfaces.consumer;

import com.github.thundax.bacon.common.log.dto.SysLogDTO;
import com.github.thundax.bacon.common.mq.BaconMqHeaders;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "bacon.mq", name = "type", havingValue = "KAFKA")
public class KafkaSysLogConsumer {

    private final SysLogMqConsumer sysLogMqConsumer;

    public KafkaSysLogConsumer(SysLogMqConsumer sysLogMqConsumer) {
        this.sysLogMqConsumer = sysLogMqConsumer;
    }

    @KafkaListener(
            topics = "${bacon.log.sys.topic:bacon-sys-log}",
            groupId = "${bacon.log.sys.consumer-group:bacon-upms-sys-log-group}")
    public void onMessage(
            @Payload SysLogDTO sysLogDTO, @Header(name = BaconMqHeaders.TENANT_ID, required = false) Long tenantId) {
        sysLogMqConsumer.consume(tenantId, sysLogDTO);
    }
}
