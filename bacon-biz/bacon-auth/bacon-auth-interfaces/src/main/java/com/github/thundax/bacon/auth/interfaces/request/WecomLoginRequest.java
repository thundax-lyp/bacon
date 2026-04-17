package com.github.thundax.bacon.auth.interfaces.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "企微登录请求")
public class WecomLoginRequest {

    @Schema(description = "企微授权码", example = "wecom-auth-code")
    @NotBlank(message = "code: must not be blank")
    private String code;
}
