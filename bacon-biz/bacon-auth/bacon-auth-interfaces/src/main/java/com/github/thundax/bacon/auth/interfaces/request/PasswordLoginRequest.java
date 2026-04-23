package com.github.thundax.bacon.auth.interfaces.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "账号密码登录请求")
public class PasswordLoginRequest {

    @Schema(description = "租户编号", example = "tenant-demo")
    @NotBlank(message = "tenantCode: must not be blank")
    private String tenantCode;

    @Schema(description = "登录账号", example = "admin")
    @NotBlank(message = "account: must not be blank")
    @Size(max = 64, message = "account: size must be between 1 and 64")
    private String account;

    @Schema(description = "RSA 加密后的密码密文", example = "Base64CipherText")
    @NotBlank(message = "password: must not be blank")
    private String password;

    @Schema(description = "RSA 密钥 ID", example = "d6ac4a47-6a7f-49d1-8ee2-3184a0d76d8f")
    @NotBlank(message = "rsaKeyId: must not be blank")
    private String rsaKeyId;

    @Schema(description = "验证码键", example = "efcd8a8f-475c-40d4-a4fd-aafdd01fcd88")
    @NotBlank(message = "captchaKey: must not be blank")
    private String captchaKey;

    @Schema(description = "验证码", example = "123456")
    @NotBlank(message = "captchaCode: must not be blank")
    @Pattern(regexp = "\\d{4,6}", message = "captchaCode: must be 4 to 6 digits")
    private String captchaCode;
}
