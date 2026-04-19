package com.github.thundax.bacon.upms.interfaces.request;

import jakarta.validation.constraints.NotBlank;
import java.util.Set;

public record RoleResourceAssignRequest(
        Set<@NotBlank(message = "resourceCodes item must not be blank") String> resourceCodes) {}
