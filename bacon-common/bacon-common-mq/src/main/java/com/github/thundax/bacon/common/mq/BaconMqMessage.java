package com.github.thundax.bacon.common.mq;

public class BaconMqMessage {

    private final String topic;
    private final String tag;
    private final String key;
    private final String exchange;
    private final String routingKey;
    private final Object payload;

    public BaconMqMessage(String topic, String tag, String key, String exchange, String routingKey, Object payload) {
        this.topic = topic;
        this.tag = tag;
        this.key = key;
        this.exchange = exchange;
        this.routingKey = routingKey;
        this.payload = payload;
    }

    public static BaconMqMessage forTopic(String topic, String key, Object payload) {
        return new BaconMqMessage(topic, null, key, null, null, payload);
    }

    public static BaconMqMessage forTopicWithTag(String topic, String tag, String key, Object payload) {
        return new BaconMqMessage(topic, tag, key, null, null, payload);
    }

    public static BaconMqMessage forExchange(String exchange, String routingKey, String key, Object payload) {
        return new BaconMqMessage(null, null, key, exchange, routingKey, payload);
    }

    public String getTopic() {
        return topic;
    }

    public String getTag() {
        return tag;
    }

    public String getKey() {
        return key;
    }

    public String getExchange() {
        return exchange;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public Object getPayload() {
        return payload;
    }
}
