package com.github.thundax.bacon.common.mq;

public interface BaconMqSender {

    void send(BaconMqMessage message);
}
