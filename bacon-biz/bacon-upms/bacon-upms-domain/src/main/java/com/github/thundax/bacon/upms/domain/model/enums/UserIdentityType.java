package com.github.thundax.bacon.upms.domain.model.enums;

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

    public static UserIdentityType fromValue(String value) {
        return value == null ? null : valueOf(value);
    }
}
