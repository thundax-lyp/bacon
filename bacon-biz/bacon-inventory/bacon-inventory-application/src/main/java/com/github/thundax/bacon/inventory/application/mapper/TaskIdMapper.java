package com.github.thundax.bacon.inventory.application.mapper;

import com.github.thundax.bacon.inventory.domain.model.valueobject.TaskId;

public final class TaskIdMapper {

    private TaskIdMapper() {
    }

    public static TaskId toDomain(Long value) {
        if (value == null) {
            return null;
        }
        return TaskId.of(value);
    }

    public static Long toValue(TaskId taskId) {
        return taskId == null ? null : taskId.value();
    }
}
