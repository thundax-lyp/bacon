package com.github.thundax.bacon.auth.interfaces.controller;

import com.github.thundax.bacon.auth.application.command.PasswordLoginCommand;
import com.github.thundax.bacon.auth.application.result.PasswordLoginChallengeResult;
import com.github.thundax.bacon.auth.application.command.LoginApplicationService;
import com.github.thundax.bacon.auth.application.command.PasswordApplicationService;
import com.github.thundax.bacon.auth.application.command.SessionApplicationService;
import com.github.thundax.bacon.auth.application.command.TokenApplicationService;
import com.github.thundax.bacon.auth.interfaces.dto.PasswordChangeRequest;
import com.github.thundax.bacon.auth.interfaces.dto.PasswordLoginRequest;
import com.github.thundax.bacon.auth.interfaces.dto.SmsLoginRequest;
import com.github.thundax.bacon.auth.interfaces.dto.TokenRefreshRequest;
import com.github.thundax.bacon.auth.interfaces.dto.WecomLoginRequest;
import com.github.thundax.bacon.auth.interfaces.response.CurrentSessionResponse;
import com.github.thundax.bacon.auth.interfaces.response.PasswordLoginChallengeResponse;
import com.github.thundax.bacon.auth.interfaces.response.UserLoginResponse;
import com.github.thundax.bacon.auth.interfaces.response.UserTokenRefreshResponse;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.common.web.util.BearerTokenUtils;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@WrappedApiController
@RequestMapping("/auth")
@Tag(name = "Auth-Management", description = "认证、登录、令牌与会话接口")
public class AuthController {

    private final LoginApplicationService loginApplicationService;
    private final TokenApplicationService tokenApplicationService;
    private final SessionApplicationService sessionApplicationService;
    private final PasswordApplicationService passwordApplicationService;

    public AuthController(LoginApplicationService loginApplicationService,
                          TokenApplicationService tokenApplicationService,
                          SessionApplicationService sessionApplicationService,
                          PasswordApplicationService passwordApplicationService) {
        this.loginApplicationService = loginApplicationService;
        this.tokenApplicationService = tokenApplicationService;
        this.sessionApplicationService = sessionApplicationService;
        this.passwordApplicationService = passwordApplicationService;
    }

    @Operation(summary = "获取账号密码登录挑战")
    @PostMapping("/login/password/challenge")
    public PasswordLoginChallengeResponse passwordLoginChallenge() {
        PasswordLoginChallengeResult result = loginApplicationService.issuePasswordLoginChallenge();
        return new PasswordLoginChallengeResponse(result.getCaptchaKey(), result.getCaptchaImageBase64(),
                result.getCaptchaExpiresIn(), result.getRsaKeyId(), result.getRsaPublicKey(), result.getRsaExpiresIn());
    }

    @Operation(summary = "账号密码登录")
    @PostMapping("/login/password")
    public UserLoginResponse passwordLogin(@Valid @RequestBody PasswordLoginRequest request) {
        return UserLoginResponse.from(loginApplicationService.loginByPassword(new PasswordLoginCommand(
                Long.parseLong(request.getTenantId().trim()), request.getAccount(), request.getPassword(), request.getRsaKeyId(),
                request.getCaptchaKey(), request.getCaptchaCode())));
    }

    @Operation(summary = "短信登录")
    @PostMapping("/login/sms")
    public UserLoginResponse smsLogin(@Valid @RequestBody SmsLoginRequest request) {
        return UserLoginResponse.from(loginApplicationService.loginBySms(request.getPhone(), request.getSmsCaptcha()));
    }

    @Operation(summary = "企微登录")
    @PostMapping("/login/wecom")
    public UserLoginResponse wecomLogin(@Valid @RequestBody WecomLoginRequest request) {
        return UserLoginResponse.from(loginApplicationService.loginByWecom(request.getCode()));
    }

    @Operation(summary = "GitHub 登录回调")
    @GetMapping("/login/github/callback")
    public UserLoginResponse githubLogin(@RequestParam("code") String code) {
        return UserLoginResponse.from(loginApplicationService.loginByGithub(code));
    }

    @Operation(summary = "刷新访问令牌")
    @PostMapping("/token/refresh")
    public UserTokenRefreshResponse refresh(@Valid @RequestBody TokenRefreshRequest request) {
        return UserTokenRefreshResponse.from(tokenApplicationService.refresh(request.getRefreshToken()));
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public void logout(@RequestHeader("Authorization") String authorization) {
        sessionApplicationService.logout(BearerTokenUtils.extractToken(authorization));
    }

    @Operation(summary = "修改当前用户密码")
    @PostMapping("/password/change")
    public void changePassword(@RequestHeader("Authorization") String authorization,
                               @Valid @RequestBody PasswordChangeRequest request) {
        passwordApplicationService.changePassword(BearerTokenUtils.extractToken(authorization),
                request.getOldPassword(), request.getNewPassword());
    }

    @Operation(summary = "获取当前会话信息")
    @GetMapping("/session/current")
    public CurrentSessionResponse currentSession(@RequestHeader("Authorization") String authorization) {
        return CurrentSessionResponse.from(
                sessionApplicationService.currentSession(BearerTokenUtils.extractToken(authorization)));
    }
}
