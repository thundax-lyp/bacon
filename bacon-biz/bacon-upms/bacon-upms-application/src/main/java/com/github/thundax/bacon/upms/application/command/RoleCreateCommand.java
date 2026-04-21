package com.github.thundax.bacon.upms.application.command;

import com.github.thundax.bacon.upms.domain.model.enums.RoleDataScopeType;
import com.github.thundax.bacon.upms.domain.model.enums.RoleType;
import com.github.thundax.bacon.upms.domain.model.valueobject.RoleCode;

public record RoleCreateCommand(RoleCode code, String name, RoleType roleType, RoleDataScopeType dataScopeType) {}
