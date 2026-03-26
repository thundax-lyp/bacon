package com.github.thundax.bacon.upms.interfaces.consumer;

import com.github.thundax.bacon.common.log.dto.SysLogDTO;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
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
            groupId = "${bacon.log.sys.consumer-group:bacon-upms-sys-log-group}"
    )
    public void onMessage(SysLogDTO sysLogDTO) {
        sysLogMqConsumer.consume(sysLogDTO);
    }
}
