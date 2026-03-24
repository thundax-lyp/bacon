package com.github.thundax.bacon.upms.interfaces.response;

import java.util.Set;

public record RoleDataScopeResponse(String dataScopeType, Set<Long> departmentIds) {
}
