package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.common.id.domain.UserId;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import java.util.List;

public record UserRoleAssignCommand(UserId userId, List<RoleId> roleIds) {}
