package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.upms.domain.model.enums.RoleStatus;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;

public record RoleStatusUpdateCommand(RoleId roleId, RoleStatus status) {}
