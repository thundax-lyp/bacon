package com.github.thundax.bacon.auth.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordLoginChallengeResult {

    private String captchaKey;
    private String captchaCode;
    private long captchaExpiresIn;
    private String rsaKeyId;
    private String rsaPublicKey;
    private long rsaExpiresIn;
}
