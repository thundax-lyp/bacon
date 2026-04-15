package com.github.thundax.bacon.upms.domain.model.valueobject;

import com.fasterxml.jackson.annotation.JsonCreator;

public record DepartmentCode(String value) {

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static DepartmentCode of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("departmentCode must not be blank");
        }
        return new DepartmentCode(value.trim());
    }
}
