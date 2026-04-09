package com.github.thundax.bacon.inventory.application.codec;

import com.github.thundax.bacon.inventory.domain.model.valueobject.TaskNo;

public final class TaskNoCodec {

    private TaskNoCodec() {
    }

    public static TaskNo toDomain(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return TaskNo.of(value);
    }

    public static String toValue(TaskNo taskNo) {
        return taskNo == null ? null : taskNo.value();
    }
}
