package com.github.thundax.bacon.common.id.mapper;

import com.github.thundax.bacon.common.id.domain.OperatorId;

public final class OperatorIdMapper {

    private OperatorIdMapper() {
    }

    public static OperatorId toDomain(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return OperatorId.of(value);
    }

    public static String toValue(OperatorId operatorId) {
        return operatorId == null ? null : operatorId.value();
    }
}
