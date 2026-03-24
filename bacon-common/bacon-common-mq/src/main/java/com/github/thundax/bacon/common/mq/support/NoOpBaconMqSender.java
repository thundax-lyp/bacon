package com.github.thundax.bacon.common.mq.support;

import com.github.thundax.bacon.common.mq.BaconMqMessage;
import com.github.thundax.bacon.common.mq.BaconMqSender;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NoOpBaconMqSender implements BaconMqSender {

    @Override
    public void send(BaconMqMessage message) {
        log.debug("MQ disabled or no implementation matched, drop message key={}", message.getKey());
    }
}
