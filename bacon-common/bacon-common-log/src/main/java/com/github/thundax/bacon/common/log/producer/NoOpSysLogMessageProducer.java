package com.github.thundax.bacon.common.log.producer;

import com.github.thundax.bacon.common.log.dto.SysLogDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NoOpSysLogMessageProducer implements SysLogMessageProducer {

    @Override
    public void send(SysLogDTO message) {
        log.debug("No MQ producer configured for sys log message, drop traceId={}", message.getTraceId());
    }
}
