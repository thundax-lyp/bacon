package com.github.thundax.bacon.auth.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "账号密码登录请求")
public class PasswordLoginRequest {

    @Schema(description = "登录账号", example = "admin")
    private String account;

    @Schema(description = "登录密码", example = "123456")
    private String password;
}
