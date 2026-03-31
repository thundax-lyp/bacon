package com.github.thundax.bacon.auth.application.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordLoginCommand {

    private String tenantId;
    private String account;
    private String password;
    private String rsaKeyId;
    private String captchaKey;
    private String captchaCode;
}
