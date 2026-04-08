package com.github.thundax.bacon.inventory.domain.model.valueobject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.thundax.bacon.common.id.core.BaseLongId;

/**
 * 回放任务主键。
 */
public final class TaskId extends BaseLongId {

    private TaskId(Long value) {
        super(value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static TaskId of(Long value) {
        return new TaskId(value);
    }
}
