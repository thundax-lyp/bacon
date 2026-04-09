package com.github.thundax.bacon.inventory.application.codec;

import com.github.thundax.bacon.inventory.domain.model.valueobject.TaskId;

public final class TaskIdCodec {

    private TaskIdCodec() {
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
