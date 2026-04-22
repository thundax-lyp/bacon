package com.github.thundax.bacon.auth.interfaces.assembler;

import com.github.thundax.bacon.auth.application.command.PasswordLoginCommand;
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

    public static SmsLoginInput toSmsLoginInput(SmsLoginRequest request) {
        return new SmsLoginInput(request.getPhone(), request.getSmsCaptcha());
    }

    public static WecomLoginInput toWecomLoginInput(WecomLoginRequest request) {
        return new WecomLoginInput(request.getCode());
    }

    public static GithubLoginInput toGithubLoginInput(String code) {
        return new GithubLoginInput(code);
    }

    public record SmsLoginInput(String phone, String smsCaptcha) {}

    public record WecomLoginInput(String code) {}

    public record GithubLoginInput(String code) {}
}
