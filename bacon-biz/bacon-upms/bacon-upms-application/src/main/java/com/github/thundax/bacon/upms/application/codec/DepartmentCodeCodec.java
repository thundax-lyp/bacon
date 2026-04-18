package com.github.thundax.bacon.upms.application.codec;

import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentCode;

public final class DepartmentCodeCodec {

    private DepartmentCodeCodec() {}

    public static DepartmentCode toDomain(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return DepartmentCode.of(value.trim());
    }

    public static String toValue(DepartmentCode value) {
        return value == null ? null : value.value();
    }
}
