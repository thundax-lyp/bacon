package com.github.thundax.bacon.common.log.producer;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bacon.log.sys")
public class BaconMqSysLogProperties {

    private String topic = "bacon-sys-log";
    private String tag = "sys-log";
    private String exchange = "bacon.sys.log.exchange";
    private String routingKey = "bacon.sys.log";

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }
}
