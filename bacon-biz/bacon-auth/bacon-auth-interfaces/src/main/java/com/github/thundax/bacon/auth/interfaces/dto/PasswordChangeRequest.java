package com.github.thundax.bacon.auth.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "修改密码请求")
public class PasswordChangeRequest {

    @Schema(description = "旧密码", example = "old-password")
    private String oldPassword;

    @Schema(description = "新密码", example = "new-password")
    private String newPassword;
}
