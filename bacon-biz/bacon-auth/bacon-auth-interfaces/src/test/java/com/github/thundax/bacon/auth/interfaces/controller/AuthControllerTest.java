package com.github.thundax.bacon.auth.interfaces.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.thundax.bacon.auth.application.command.GithubLoginCommand;
import com.github.thundax.bacon.auth.application.command.LoginApplicationService;
import com.github.thundax.bacon.auth.application.command.PasswordCommandApplicationService;
import com.github.thundax.bacon.auth.application.command.PasswordLoginCommand;
import com.github.thundax.bacon.auth.application.command.SmsLoginCommand;
import com.github.thundax.bacon.auth.application.command.SessionCommandApplicationService;
import com.github.thundax.bacon.auth.application.command.TokenCommandApplicationService;
import com.github.thundax.bacon.auth.application.command.WecomLoginCommand;
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
    void shouldDelegateSmsLoginWithAssembledCommand() {
        LoginApplicationService loginApplicationService = mock(LoginApplicationService.class);
        when(loginApplicationService.loginBySms(any())).thenReturn(loginResult());

        AuthController controller = new AuthController(
                loginApplicationService,
                mock(TokenCommandApplicationService.class),
                mock(SessionCommandApplicationService.class),
                mock(SessionQueryApplicationService.class),
                mock(PasswordCommandApplicationService.class));

        UserLoginResponse response = controller.smsLogin(new SmsLoginRequest("13800000000", "123456"));

        ArgumentCaptor<SmsLoginCommand> commandCaptor = ArgumentCaptor.forClass(SmsLoginCommand.class);
        verify(loginApplicationService).loginBySms(commandCaptor.capture());
        SmsLoginCommand command = commandCaptor.getValue();
        assertEquals("13800000000", command.getPhone());
        assertEquals("123456", command.getSmsCaptcha());
        assertEquals("access-token", response.accessToken());
    }

    @Test
    void shouldDelegateWecomLoginWithAssembledCommand() {
        LoginApplicationService loginApplicationService = mock(LoginApplicationService.class);
        when(loginApplicationService.loginByWecom(any())).thenReturn(loginResult());

        AuthController controller = new AuthController(
                loginApplicationService,
                mock(TokenCommandApplicationService.class),
                mock(SessionCommandApplicationService.class),
                mock(SessionQueryApplicationService.class),
                mock(PasswordCommandApplicationService.class));

        UserLoginResponse response = controller.wecomLogin(new WecomLoginRequest("wecom-code"));

        ArgumentCaptor<WecomLoginCommand> commandCaptor = ArgumentCaptor.forClass(WecomLoginCommand.class);
        verify(loginApplicationService).loginByWecom(commandCaptor.capture());
        WecomLoginCommand command = commandCaptor.getValue();
        assertEquals("wecom-code", command.getCode());
        assertEquals("access-token", response.accessToken());
    }

    @Test
    void shouldDelegateGithubLoginWithAssembledCommand() {
        LoginApplicationService loginApplicationService = mock(LoginApplicationService.class);
        when(loginApplicationService.loginByGithub(any())).thenReturn(loginResult());

        AuthController controller = new AuthController(
                loginApplicationService,
                mock(TokenCommandApplicationService.class),
                mock(SessionCommandApplicationService.class),
                mock(SessionQueryApplicationService.class),
                mock(PasswordCommandApplicationService.class));

        UserLoginResponse response = controller.githubLogin("github-code");

        ArgumentCaptor<GithubLoginCommand> commandCaptor = ArgumentCaptor.forClass(GithubLoginCommand.class);
        verify(loginApplicationService).loginByGithub(commandCaptor.capture());
        GithubLoginCommand command = commandCaptor.getValue();
        assertEquals("github-code", command.getCode());
        assertEquals("access-token", response.accessToken());
    }

    private UserLoginDTO loginResult() {
        return new UserLoginDTO("access-token", "refresh-token", "Bearer", 1800L, "session-id", 2001L, 1001L, false);
    }
}
