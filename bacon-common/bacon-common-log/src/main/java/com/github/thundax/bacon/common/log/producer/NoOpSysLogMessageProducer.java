package com.github.thundax.bacon.common.log.producer;

import com.github.thundax.bacon.common.log.dto.SysLogDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnMissingBean(SysLogMessageProducer.class)
public class NoOpSysLogMessageProducer implements SysLogMessageProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(NoOpSysLogMessageProducer.class);

    @Override
    public void send(SysLogDTO message) {
        LOGGER.debug("No MQ producer configured for sys log message, drop traceId={}", message.getTraceId());
    }
}
