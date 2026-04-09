package com.github.thundax.bacon.common.test.architecture.fixture.domain.model.enums;

import java.util.Arrays;

public enum InvalidSimpleEnumFixture {
    ENABLED,
    DISABLED;

    public String value() {
        return name();
    }

    public static InvalidSimpleEnumFixture fromValue(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown simple enum fixture: " + value));
    }
}
