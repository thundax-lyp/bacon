package com.github.thundax.bacon.upms.domain.model.enums;

public enum PostStatus {

    ENABLED,
    DISABLED;

    public String value() {
        return name();
    }

    public static PostStatus fromValue(String value) {
        return value == null ? null : valueOf(value);
    }
}
