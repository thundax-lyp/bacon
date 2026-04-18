package com.github.thundax.bacon.upms.application.codec;

import com.github.thundax.bacon.upms.domain.model.valueobject.RoleCode;

public final class RoleCodeCodec {

    private RoleCodeCodec() {}

    public static RoleCode toDomain(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return RoleCode.of(value.trim());
    }

    public static String toValue(RoleCode value) {
        return value == null ? null : value.value();
    }
}
