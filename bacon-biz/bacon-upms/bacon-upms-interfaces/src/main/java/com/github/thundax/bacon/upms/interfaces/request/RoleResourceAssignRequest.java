package com.github.thundax.bacon.upms.interfaces.request;

import java.util.Set;

public record RoleResourceAssignRequest(Set<String> resourceCodes) {}
