package com.github.thundax.bacon.upms.api.response;

import java.util.Set;

public record UserDataScopeFacadeResponse(boolean allAccess, Set<String> scopeTypes, Set<Long> departmentIds) {}
