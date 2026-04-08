package com.github.thundax.bacon.inventory.application.mapper;

import com.github.thundax.bacon.inventory.domain.model.valueobject.TaskNo;

public final class TaskNoMapper {

    private TaskNoMapper() {
    }

    public static TaskNo toDomain(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return TaskNo.of(value);
    }
}
