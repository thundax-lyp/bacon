package com.github.thundax.bacon.upms.domain.model.valueobject;

import com.fasterxml.jackson.annotation.JsonCreator;

public record SysLogId(Long value) {

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static SysLogId of(Long value) {
        if (value == null) {
            throw new IllegalArgumentException("sysLogId must not be null");
        }
        return new SysLogId(value);
    }
}
