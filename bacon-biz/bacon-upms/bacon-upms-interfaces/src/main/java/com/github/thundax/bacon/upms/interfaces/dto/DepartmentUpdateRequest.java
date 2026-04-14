package com.github.thundax.bacon.upms.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DepartmentUpdateRequest(
        @NotBlank(message = "code must not be blank") @Size(max = 64, message = "code length must be <= 64")
                String code,
        @NotBlank(message = "name must not be blank") @Size(max = 128, message = "name length must be <= 128")
                String name,
        Long parentId,
        Long leaderUserId,
        Integer sort) {}
