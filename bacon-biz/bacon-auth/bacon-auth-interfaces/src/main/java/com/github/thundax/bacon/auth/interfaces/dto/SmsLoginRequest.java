package com.github.thundax.bacon.auth.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "短信登录请求")
public class SmsLoginRequest {

    @Schema(description = "手机号", example = "13800000000")
    private String phone;

    @Schema(description = "短信验证码", example = "123456")
    private String smsCaptcha;
}
