package com.github.thundax.bacon.upms.domain.model.enums;

public enum ResourceType {

    API,
    RPC,
    EVENT;

    public String value() {
        return name();
    }

    public static ResourceType fromValue(String value) {
        return value == null ? null : valueOf(value);
    }
}
