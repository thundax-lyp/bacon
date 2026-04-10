package com.github.thundax.bacon.common.mq;

import java.util.LinkedHashMap;
import java.util.Map;

public class BaconMqMessage {

    private final String topic;
    private final String tag;
    private final String key;
    private final String exchange;
    private final String routingKey;
    private final Object payload;
    private final Map<String, String> headers;

    public BaconMqMessage(
            String topic,
            String tag,
            String key,
            String exchange,
            String routingKey,
            Object payload,
            Map<String, String> headers) {
        this.topic = topic;
        this.tag = tag;
        this.key = key;
        this.exchange = exchange;
        this.routingKey = routingKey;
        this.payload = payload;
        this.headers = headers == null ? new LinkedHashMap<>() : new LinkedHashMap<>(headers);
    }

    public static BaconMqMessage forTopic(String topic, String key, Object payload) {
        return new BaconMqMessage(topic, null, key, null, null, payload, Map.of());
    }

    public static BaconMqMessage forTopicWithTag(String topic, String tag, String key, Object payload) {
        return new BaconMqMessage(topic, tag, key, null, null, payload, Map.of());
    }

    public static BaconMqMessage forExchange(String exchange, String routingKey, String key, Object payload) {
        return new BaconMqMessage(null, null, key, exchange, routingKey, payload, Map.of());
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

    public Map<String, String> getHeaders() {
        return headers;
    }

    public BaconMqMessage withHeader(String name, String value) {
        LinkedHashMap<String, String> updatedHeaders = new LinkedHashMap<>(headers);
        if (value == null) {
            updatedHeaders.remove(name);
        } else {
            updatedHeaders.put(name, value);
        }
        return new BaconMqMessage(topic, tag, key, exchange, routingKey, payload, updatedHeaders);
    }
}
