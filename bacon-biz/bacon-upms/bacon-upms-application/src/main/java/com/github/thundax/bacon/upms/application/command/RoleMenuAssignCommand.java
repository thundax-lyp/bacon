package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.upms.domain.model.valueobject.MenuId;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleId;
import java.util.Set;

public record RoleMenuAssignCommand(RoleId roleId, Set<MenuId> menuIds) {}
