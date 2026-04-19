package com.github.thundax.bacon.upms.interfaces.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @NotBlank(message = "account must not be blank") @Size(max = 64, message = "account length must be <= 64")
                String account,
        @NotBlank(message = "name must not be blank") @Size(max = 128, message = "name length must be <= 128")
                String name,
        @NotBlank(message = "phone must not be blank")
                @Pattern(regexp = "^1\\d{10}$", message = "phone must be a valid mainland China mobile number")
                String phone,
        @NotNull(message = "departmentId must not be null")
                @Positive(message = "departmentId must be greater than 0")
                Long departmentId) {}
