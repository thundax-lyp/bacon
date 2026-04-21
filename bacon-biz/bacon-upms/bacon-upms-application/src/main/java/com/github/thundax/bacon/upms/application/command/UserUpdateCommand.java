package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;

public record UserUpdateCommand(
        UserId userId, String account, String name, String phone, DepartmentId departmentId) {}
