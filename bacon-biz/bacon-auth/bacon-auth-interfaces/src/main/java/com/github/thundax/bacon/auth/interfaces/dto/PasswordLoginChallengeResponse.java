package com.github.thundax.bacon.auth.interfaces.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "账号密码登录挑战响应")
public class PasswordLoginChallengeResponse {

    @Schema(description = "验证码键")
    private String captchaKey;

    @Schema(description = "验证码内容")
    private String captchaCode;

    @Schema(description = "验证码有效期，单位秒")
    private long captchaExpiresIn;

    @Schema(description = "RSA 密钥 ID")
    private String rsaKeyId;

    @Schema(description = "RSA 公钥，Base64 编码")
    private String rsaPublicKey;

    @Schema(description = "RSA 公钥有效期，单位秒")
    private long rsaExpiresIn;
}
