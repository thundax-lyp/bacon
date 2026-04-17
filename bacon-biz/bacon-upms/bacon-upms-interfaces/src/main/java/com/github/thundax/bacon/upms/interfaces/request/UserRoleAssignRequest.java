package com.github.thundax.bacon.upms.interfaces.request;

import java.util.List;

public record UserRoleAssignRequest(List<Long> roleIds) {}
