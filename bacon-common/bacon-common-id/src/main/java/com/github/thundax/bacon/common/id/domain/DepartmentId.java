package com.github.thundax.bacon.common.id.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.thundax.bacon.common.id.core.BaseLongId;

public final class DepartmentId extends BaseLongId {

    private DepartmentId(Long value) {
        super(value);
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static DepartmentId of(Long value) {
        return new DepartmentId(value);
    }
}
