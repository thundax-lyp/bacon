package com.github.thundax.bacon.upms.domain.model.enums;

import java.util.Arrays;

public enum UserIdentityType {

    ACCOUNT,
    EMAIL,
    PHONE,
    GITHUB,
    WECHAT,
    WECOM;

    public String value() {
        return name();
    }

    public static UserIdentityType from(String value) {
        return Arrays.stream(values())
                .filter(item -> item.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown user identity type: " + value));
    }
}
