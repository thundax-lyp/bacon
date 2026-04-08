package com.github.thundax.bacon.inventory.application.mapper;

import com.github.thundax.bacon.inventory.domain.model.valueobject.DeadLetterId;

public final class DeadLetterIdMapper {

    private DeadLetterIdMapper() {
    }

    public static DeadLetterId toDomain(Long value) {
        if (value == null) {
            return null;
        }
        return DeadLetterId.of(value);
    }

    public static Long toValue(DeadLetterId deadLetterId) {
        return deadLetterId == null ? null : deadLetterId.value();
    }
}
