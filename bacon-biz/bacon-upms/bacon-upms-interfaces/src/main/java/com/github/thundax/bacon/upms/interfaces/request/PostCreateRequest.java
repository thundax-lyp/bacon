package com.github.thundax.bacon.upms.interfaces.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PostCreateRequest(
        @NotBlank(message = "code must not be blank") @Size(max = 64, message = "code length must be <= 64")
                String code,
        @NotBlank(message = "name must not be blank") @Size(max = 128, message = "name length must be <= 128")
                String name,
        @NotNull(message = "departmentId must not be null")
                @Positive(message = "departmentId must be greater than 0")
                Long departmentId) {}
