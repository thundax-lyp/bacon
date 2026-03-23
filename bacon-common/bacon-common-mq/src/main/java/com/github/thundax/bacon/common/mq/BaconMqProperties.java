package com.github.thundax.bacon.common.mq;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bacon.mq")
public class BaconMqProperties {

    private boolean enabled = true;
    private BaconMqType type = BaconMqType.ROCKETMQ;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public BaconMqType getType() {
        return type;
    }

    public void setType(BaconMqType type) {
        this.type = type;
    }
}
