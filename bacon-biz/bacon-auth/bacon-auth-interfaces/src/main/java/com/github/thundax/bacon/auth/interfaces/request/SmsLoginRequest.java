package com.github.thundax.bacon.auth.interfaces.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "短信登录请求")
public class SmsLoginRequest {

    @Schema(description = "手机号", example = "13800000000")
    @NotBlank(message = "phone: must not be blank")
    @Pattern(regexp = "^1\\d{10}$", message = "phone: must be a valid mainland China mobile number")
    private String phone;

    @Schema(description = "短信验证码", example = "123456")
    @NotBlank(message = "smsCaptcha: must not be blank")
    @Pattern(regexp = "\\d{4,6}", message = "smsCaptcha: must be 4 to 6 digits")
    private String smsCaptcha;
}
