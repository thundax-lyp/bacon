package com.github.thundax.bacon.upms.domain.model.valueobject;

import com.github.thundax.bacon.upms.domain.model.enums.RoleDataScopeType;
import java.util.Objects;
import java.util.Set;

public record RoleDataScopeAssignment(RoleDataScopeType dataScopeType, Set<DepartmentId> departmentIds) {

    public RoleDataScopeAssignment {
        Objects.requireNonNull(dataScopeType, "dataScopeType must not be null");
        departmentIds = departmentIds == null ? Set.of() : Set.copyOf(departmentIds);
    }

    public static RoleDataScopeAssignment of(RoleDataScopeType dataScopeType, Set<DepartmentId> departmentIds) {
        return new RoleDataScopeAssignment(dataScopeType, departmentIds);
    }
}
