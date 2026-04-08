package com.github.thundax.bacon.inventory.application.mapper;

import com.github.thundax.bacon.inventory.domain.model.valueobject.OutboxId;

public final class OutboxIdMapper {

    private OutboxIdMapper() {
    }

    public static OutboxId toDomain(Long value) {
        if (value == null) {
            return null;
        }
        return OutboxId.of(value);
    }
}
