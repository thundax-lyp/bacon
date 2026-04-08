package com.github.thundax.bacon.upms.domain.model.enums;

import java.util.Arrays;

public enum ResourceType {

    API,
    RPC,
    EVENT;

    public String value() {
        return name();
    }

    public static ResourceType from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown resource type: " + value));
    }
}
