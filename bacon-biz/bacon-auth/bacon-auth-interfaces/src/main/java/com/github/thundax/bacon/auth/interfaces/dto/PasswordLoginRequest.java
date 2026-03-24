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

    @Schema(description = "租户 ID", example = "1001")
    private Long tenantId;

    @Schema(description = "登录账号", example = "admin")
    private String account;

    @Schema(description = "RSA 加密后的密码密文", example = "Base64CipherText")
    private String password;

    @Schema(description = "RSA 密钥 ID", example = "d6ac4a47-6a7f-49d1-8ee2-3184a0d76d8f")
    private String rsaKeyId;

    @Schema(description = "验证码键", example = "efcd8a8f-475c-40d4-a4fd-aafdd01fcd88")
    private String captchaKey;

    @Schema(description = "验证码", example = "123456")
    private String captchaCode;
}
