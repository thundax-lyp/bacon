package com.github.thundax.bacon.common.mq.support;

import com.github.thundax.bacon.common.mq.BaconMqMessage;
import com.github.thundax.bacon.common.mq.BaconMqSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoOpBaconMqSender implements BaconMqSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(NoOpBaconMqSender.class);

    @Override
    public void send(BaconMqMessage message) {
        LOGGER.debug("MQ disabled or no implementation matched, drop message key={}", message.getKey());
    }
}
