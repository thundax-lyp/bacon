package com.github.thundax.bacon.upms.application.codec;

import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;

public final class DepartmentIdCodec {

    private DepartmentIdCodec() {}

    public static DepartmentId toDomain(Long value) {
        return value == null ? null : DepartmentId.of(value);
    }

    public static Long toValue(DepartmentId value) {
        return value == null ? null : value.value();
    }
}
