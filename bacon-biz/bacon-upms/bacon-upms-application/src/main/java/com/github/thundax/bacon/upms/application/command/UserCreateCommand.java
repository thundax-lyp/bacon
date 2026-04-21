package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;

public record UserCreateCommand(String account, String name, String phone, DepartmentId departmentId) {}
