package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;

public record DepartmentSortUpdateCommand(DepartmentId departmentId, Integer sort) {}
