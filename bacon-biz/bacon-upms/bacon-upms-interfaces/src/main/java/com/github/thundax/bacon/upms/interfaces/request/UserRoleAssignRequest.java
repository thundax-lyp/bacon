package com.github.thundax.bacon.upms.interfaces.request;

import jakarta.validation.constraints.Positive;
import java.util.List;

public record UserRoleAssignRequest(List<@Positive(message = "roleIds item must be greater than 0") Long> roleIds) {}
