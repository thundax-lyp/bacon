package com.github.thundax.bacon.common.log.producer;

import com.github.thundax.bacon.common.log.dto.SysLogDTO;

/**
 * MQ producer abstraction for system access logs.
 */
public interface SysLogMessageProducer {

    void send(SysLogDTO message);
}
