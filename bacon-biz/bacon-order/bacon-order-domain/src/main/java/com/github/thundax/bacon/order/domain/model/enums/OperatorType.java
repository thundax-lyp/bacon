package com.github.thundax.bacon.order.domain.model.enums;

import java.util.Arrays;

/**
 * 操作人类型。
 */
public enum OperatorType {
    SYSTEM,
    USER,
    ADMIN;

    public String value() {
        return name();
    }

    public static OperatorType fromValue(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported operator type: " + value));
    }
}
