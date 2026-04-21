package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentCode;
import com.github.thundax.bacon.upms.domain.model.valueobject.DepartmentId;

public record DepartmentCreateCommand(DepartmentCode code, String name, DepartmentId parentId, UserId leaderUserId) {}
