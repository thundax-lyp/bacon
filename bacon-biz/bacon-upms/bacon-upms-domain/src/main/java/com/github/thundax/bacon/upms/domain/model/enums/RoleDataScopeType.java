package com.github.thundax.bacon.upms.domain.model.enums;

public enum RoleDataScopeType {

    ALL,
    DEPARTMENT,
    DEPARTMENT_AND_CHILDREN,
    SELF,
    CUSTOM;

    public String value() {
        return name();
    }

    public static RoleDataScopeType fromValue(String value) {
        return value == null ? null : valueOf(value);
    }
}
