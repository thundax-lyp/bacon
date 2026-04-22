package com.github.thundax.bacon.auth.interfaces.assembler;

import com.github.thundax.bacon.auth.application.command.GithubLoginCommand;
import com.github.thundax.bacon.auth.application.command.PasswordLoginCommand;
import com.github.thundax.bacon.auth.application.command.SmsLoginCommand;
import com.github.thundax.bacon.auth.application.command.WecomLoginCommand;
import com.github.thundax.bacon.auth.interfaces.request.PasswordLoginRequest;
import com.github.thundax.bacon.auth.interfaces.request.SmsLoginRequest;
import com.github.thundax.bacon.auth.interfaces.request.WecomLoginRequest;

public final class AuthInterfaceAssembler {

    private AuthInterfaceAssembler() {}

    public static PasswordLoginCommand toPasswordLoginCommand(PasswordLoginRequest request) {
        return new PasswordLoginCommand(
                Long.parseLong(request.getTenantCode().trim()),
                request.getAccount(),
                request.getPassword(),
                request.getRsaKeyId(),
                request.getCaptchaKey(),
                request.getCaptchaCode());
    }

    public static SmsLoginCommand toSmsLoginCommand(SmsLoginRequest request) {
        return new SmsLoginCommand(request.getPhone(), request.getSmsCaptcha());
    }

    public static WecomLoginCommand toWecomLoginCommand(WecomLoginRequest request) {
        return new WecomLoginCommand(request.getCode());
    }

    public static GithubLoginCommand toGithubLoginCommand(String code) {
        return new GithubLoginCommand(code);
    }
}
