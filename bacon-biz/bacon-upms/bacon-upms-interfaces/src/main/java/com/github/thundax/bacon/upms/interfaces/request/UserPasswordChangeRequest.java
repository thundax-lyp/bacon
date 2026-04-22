package com.github.thundax.bacon.upms.interfaces.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 当前用户修改密码请求。
 */
public record UserPasswordChangeRequest(
        @NotBlank(message = "oldPassword must not be blank") String oldPassword,
        @NotBlank(message = "newPassword must not be blank")
                @Size(min = 8, max = 64, message = "newPassword size must be between 8 and 64")
                String newPassword) {}
