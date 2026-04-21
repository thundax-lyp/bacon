package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.upms.domain.model.enums.RoleDataScopeType;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import java.util.Set;

public record RoleDataScopeAssignCommand(RoleId roleId, RoleDataScopeType dataScopeType, Set<DepartmentId> departmentIds) {}
