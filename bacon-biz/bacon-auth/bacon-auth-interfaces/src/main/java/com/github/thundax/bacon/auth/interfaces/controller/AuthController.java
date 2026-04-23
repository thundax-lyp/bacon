package com.github.thundax.bacon.auth.interfaces.controller;

import com.github.thundax.bacon.auth.application.command.LoginApplicationService;
import com.github.thundax.bacon.auth.application.command.PasswordChangeCommand;
import com.github.thundax.bacon.auth.application.command.PasswordCommandApplicationService;
import com.github.thundax.bacon.auth.application.command.SessionCommandApplicationService;
import com.github.thundax.bacon.auth.application.command.SessionLogoutCommand;
import com.github.thundax.bacon.auth.application.command.TokenCommandApplicationService;
import com.github.thundax.bacon.auth.application.command.TokenRefreshCommand;
import com.github.thundax.bacon.auth.application.query.SessionCurrentQuery;
import com.github.thundax.bacon.auth.application.query.SessionQueryApplicationService;
import com.github.thundax.bacon.auth.application.result.PasswordLoginChallengeResult;
import com.github.thundax.bacon.auth.interfaces.assembler.AuthInterfaceAssembler;
import com.github.thundax.bacon.auth.interfaces.request.PasswordChangeRequest;
import com.github.thundax.bacon.auth.interfaces.request.PasswordLoginRequest;
import com.github.thundax.bacon.auth.interfaces.request.SmsLoginRequest;
import com.github.thundax.bacon.auth.interfaces.request.TokenRefreshRequest;
import com.github.thundax.bacon.auth.interfaces.request.WecomLoginRequest;
import com.github.thundax.bacon.auth.interfaces.response.CurrentSessionResponse;
import com.github.thundax.bacon.auth.interfaces.response.PasswordLoginChallengeResponse;
import com.github.thundax.bacon.auth.interfaces.response.UserLoginResponse;
import com.github.thundax.bacon.auth.interfaces.response.UserTokenRefreshResponse;
import com.github.thundax.bacon.common.web.annotation.ApiAnnotationException;
import com.github.thundax.bacon.common.web.annotation.ApiAnnotationExceptionBucket;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.common.web.util.BearerTokenUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

@RestController
@WrappedApiController
@RequestMapping("/auth/authentications")
@Validated
@Tag(name = "Auth-Management", description = "认证、登录、令牌与会话接口")
@ApiAnnotationException(bucket = ApiAnnotationExceptionBucket.AUTH_PUBLIC)
public class AuthController {

    private final LoginApplicationService loginApplicationService;
    private final TokenCommandApplicationService tokenCommandApplicationService;
    private final SessionCommandApplicationService sessionCommandApplicationService;
    private final SessionQueryApplicationService sessionQueryApplicationService;
    private final PasswordCommandApplicationService passwordCommandApplicationService;

    public AuthController(
            LoginApplicationService loginApplicationService,
            TokenCommandApplicationService tokenCommandApplicationService,
            SessionCommandApplicationService sessionCommandApplicationService,
            SessionQueryApplicationService sessionQueryApplicationService,
            PasswordCommandApplicationService passwordCommandApplicationService) {
        this.loginApplicationService = loginApplicationService;
        this.tokenCommandApplicationService = tokenCommandApplicationService;
        this.sessionCommandApplicationService = sessionCommandApplicationService;
        this.sessionQueryApplicationService = sessionQueryApplicationService;
        this.passwordCommandApplicationService = passwordCommandApplicationService;
    }

    @Operation(summary = "获取账号密码登录挑战")
    @PostMapping("/logins/password-challenge")
    public PasswordLoginChallengeResponse passwordLoginChallenge() {
        PasswordLoginChallengeResult result = loginApplicationService.issuePasswordLoginChallenge();
        return new PasswordLoginChallengeResponse(
                result.getCaptchaKey(),
                result.getCaptchaImageBase64(),
                result.getCaptchaExpiresIn(),
                result.getRsaKeyId(),
                result.getRsaPublicKey(),
                result.getRsaExpiresIn());
    }

    @Operation(summary = "账号密码登录")
    @PostMapping("/logins/password")
    public UserLoginResponse passwordLogin(@Valid @RequestBody PasswordLoginRequest request) {
        return UserLoginResponse.from(
                loginApplicationService.loginByPassword(AuthInterfaceAssembler.toPasswordLoginCommand(request)));
    }

    @Operation(summary = "短信登录")
    @PostMapping("/logins/sms")
    public UserLoginResponse smsLogin(@Valid @RequestBody SmsLoginRequest request) {
        return UserLoginResponse.from(loginApplicationService.loginBySms(AuthInterfaceAssembler.toSmsLoginCommand(request)));
    }

    @Operation(summary = "企微登录")
    @PostMapping("/logins/wecom")
    public UserLoginResponse wecomLogin(@Valid @RequestBody WecomLoginRequest request) {
        return UserLoginResponse.from(loginApplicationService.loginByWecom(AuthInterfaceAssembler.toWecomLoginCommand(request)));
    }

    @Operation(summary = "GitHub 登录回调")
    @GetMapping("/logins/github-callback")
    public UserLoginResponse githubLogin(@RequestParam("code") @NotBlank(message = "code must not be blank") String code) {
        return UserLoginResponse.from(loginApplicationService.loginByGithub(AuthInterfaceAssembler.toGithubLoginCommand(code)));
    }

    @Operation(summary = "刷新访问令牌")
    @PostMapping("/tokens/refresh")
    public UserTokenRefreshResponse refresh(@Valid @RequestBody TokenRefreshRequest request) {
        return UserTokenRefreshResponse.from(
                tokenCommandApplicationService.refresh(new TokenRefreshCommand(request.getRefreshToken())));
    }

    @Operation(summary = "退出登录")
    @PostMapping("/sessions/logout")
    public void logout(
            @RequestHeader("Authorization") @NotBlank(message = "Authorization header must not be blank")
                    String authorization) {
        sessionCommandApplicationService.logout(new SessionLogoutCommand(BearerTokenUtils.extractToken(authorization)));
    }

    @Operation(summary = "修改当前用户密码")
    @PostMapping("/passwords/change")
    public void changePassword(
            @RequestHeader("Authorization") @NotBlank(message = "Authorization header must not be blank")
                    String authorization,
            @Valid @RequestBody PasswordChangeRequest request) {
        passwordCommandApplicationService.changePassword(new PasswordChangeCommand(
                BearerTokenUtils.extractToken(authorization), request.getOldPassword(), request.getNewPassword()));
    }

    @Operation(summary = "获取当前会话信息")
    @GetMapping("/sessions/current")
    public CurrentSessionResponse currentSession(
            @RequestHeader("Authorization") @NotBlank(message = "Authorization header must not be blank")
                    String authorization) {
        return CurrentSessionResponse.from(
                sessionQueryApplicationService.currentSession(new SessionCurrentQuery(BearerTokenUtils.extractToken(authorization))));
    }
}
