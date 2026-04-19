package com.github.thundax.bacon.upms.interfaces.request;

import jakarta.validation.constraints.Positive;
import java.util.Set;

public record RoleMenuAssignRequest(Set<@Positive(message = "menuIds item must be greater than 0") Long> menuIds) {}
