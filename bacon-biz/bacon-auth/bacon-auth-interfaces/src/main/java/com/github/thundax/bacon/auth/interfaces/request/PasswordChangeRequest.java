package com.github.thundax.bacon.auth.interfaces.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "修改密码请求")
public class PasswordChangeRequest {

    @Schema(description = "旧密码", example = "old-password")
    @NotBlank(message = "oldPassword: must not be blank")
    private String oldPassword;

    @Schema(description = "新密码", example = "new-password")
    @NotBlank(message = "newPassword: must not be blank")
    @Size(min = 8, max = 64, message = "newPassword: size must be between 8 and 64")
    private String newPassword;
}
