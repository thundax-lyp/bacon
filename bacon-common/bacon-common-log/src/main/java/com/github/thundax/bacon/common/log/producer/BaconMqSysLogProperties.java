package com.github.thundax.bacon.common.log.producer;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bacon.log.sys")
public class BaconMqSysLogProperties {

    private String topic = "bacon-sys-log";
    private String tag = "sys-log";
    private String exchange = "bacon.sys.log.exchange";
    private String queue = "bacon.sys.log.queue";
    private String routingKey = "bacon.sys.log";
    private String consumerGroup = "bacon-upms-sys-log-group";

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

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }
}
