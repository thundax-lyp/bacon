package com.github.thundax.bacon.auth.interfaces.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.bacon.auth.application.command.LoginApplicationService;
import com.github.thundax.bacon.auth.application.command.PasswordCommandApplicationService;
import com.github.thundax.bacon.auth.application.command.PasswordLoginCommand;
import com.github.thundax.bacon.auth.application.command.SessionCommandApplicationService;
import com.github.thundax.bacon.auth.application.command.TokenCommandApplicationService;
import com.github.thundax.bacon.auth.application.dto.UserLoginDTO;
import com.github.thundax.bacon.auth.application.query.SessionQueryApplicationService;
import com.github.thundax.bacon.auth.interfaces.request.PasswordLoginRequest;
import com.github.thundax.bacon.auth.interfaces.request.SmsLoginRequest;
import com.github.thundax.bacon.auth.interfaces.request.WecomLoginRequest;
import com.github.thundax.bacon.auth.interfaces.response.UserLoginResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AuthControllerTest {

    @Test
    void shouldDelegatePasswordLoginWithAssembledCommand() {
        LoginApplicationService loginApplicationService = mock(LoginApplicationService.class);
        when(loginApplicationService.loginByPassword(any())).thenReturn(loginResult());

        AuthController controller = new AuthController(
                loginApplicationService,
                mock(TokenCommandApplicationService.class),
                mock(SessionCommandApplicationService.class),
                mock(SessionQueryApplicationService.class),
                mock(PasswordCommandApplicationService.class));

        UserLoginResponse response = controller.passwordLogin(
                new PasswordLoginRequest(" 1001 ", "demo", "cipher", "rsa-key", "captcha-key", "123456"));

        ArgumentCaptor<PasswordLoginCommand> commandCaptor = ArgumentCaptor.forClass(PasswordLoginCommand.class);
        verify(loginApplicationService).loginByPassword(commandCaptor.capture());
        PasswordLoginCommand command = commandCaptor.getValue();
        assertEquals(1001L, command.getTenantId());
        assertEquals("demo", command.getAccount());
        assertEquals("cipher", command.getPassword());
        assertEquals("rsa-key", command.getRsaKeyId());
        assertEquals("captcha-key", command.getCaptchaKey());
        assertEquals("123456", command.getCaptchaCode());
        assertEquals("access-token", response.accessToken());
    }

    @Test
    void shouldDelegateSmsLoginWithAssembledInput() {
        LoginApplicationService loginApplicationService = mock(LoginApplicationService.class);
        when(loginApplicationService.loginBySms("13800000000", "123456")).thenReturn(loginResult());

        AuthController controller = new AuthController(
                loginApplicationService,
                mock(TokenCommandApplicationService.class),
                mock(SessionCommandApplicationService.class),
                mock(SessionQueryApplicationService.class),
                mock(PasswordCommandApplicationService.class));

        UserLoginResponse response = controller.smsLogin(new SmsLoginRequest("13800000000", "123456"));

        verify(loginApplicationService).loginBySms("13800000000", "123456");
        assertEquals("access-token", response.accessToken());
    }

    @Test
    void shouldDelegateWecomLoginWithAssembledInput() {
        LoginApplicationService loginApplicationService = mock(LoginApplicationService.class);
        when(loginApplicationService.loginByWecom("wecom-code")).thenReturn(loginResult());

        AuthController controller = new AuthController(
                loginApplicationService,
                mock(TokenCommandApplicationService.class),
                mock(SessionCommandApplicationService.class),
                mock(SessionQueryApplicationService.class),
                mock(PasswordCommandApplicationService.class));

        UserLoginResponse response = controller.wecomLogin(new WecomLoginRequest("wecom-code"));

        verify(loginApplicationService).loginByWecom("wecom-code");
        assertEquals("access-token", response.accessToken());
    }

    @Test
    void shouldDelegateGithubLoginWithAssembledInput() {
        LoginApplicationService loginApplicationService = mock(LoginApplicationService.class);
        when(loginApplicationService.loginByGithub("github-code")).thenReturn(loginResult());

        AuthController controller = new AuthController(
                loginApplicationService,
                mock(TokenCommandApplicationService.class),
                mock(SessionCommandApplicationService.class),
                mock(SessionQueryApplicationService.class),
                mock(PasswordCommandApplicationService.class));

        UserLoginResponse response = controller.githubLogin("github-code");

        verify(loginApplicationService).loginByGithub("github-code");
        assertEquals("access-token", response.accessToken());
    }

    private UserLoginDTO loginResult() {
        return new UserLoginDTO("access-token", "refresh-token", "Bearer", 1800L, "session-id", 2001L, 1001L, false);
    }
}
