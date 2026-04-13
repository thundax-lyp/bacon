package com.github.thundax.bacon.upms.application.codec;

import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;

public final class RoleIdCodec {

    private RoleIdCodec() {}

    public static RoleId toDomain(Long value) {
        return value == null ? null : RoleId.of(value);
    }

    public static Long toValue(RoleId value) {
        return value == null ? null : value.value();
    }
}
