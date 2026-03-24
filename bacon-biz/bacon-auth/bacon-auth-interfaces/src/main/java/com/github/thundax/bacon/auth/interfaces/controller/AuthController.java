package com.github.thundax.bacon.auth.interfaces.controller;

import com.github.thundax.bacon.auth.api.dto.CurrentSessionDTO;
import com.github.thundax.bacon.auth.api.dto.UserLoginDTO;
import com.github.thundax.bacon.auth.api.dto.UserTokenRefreshDTO;
import com.github.thundax.bacon.auth.application.command.PasswordLoginCommand;
import com.github.thundax.bacon.auth.application.result.PasswordLoginChallengeResult;
import com.github.thundax.bacon.auth.application.service.LoginApplicationService;
import com.github.thundax.bacon.auth.application.service.PasswordApplicationService;
import com.github.thundax.bacon.auth.application.service.SessionApplicationService;
import com.github.thundax.bacon.auth.application.service.TokenApplicationService;
import com.github.thundax.bacon.auth.interfaces.dto.PasswordChangeRequest;
import com.github.thundax.bacon.auth.interfaces.dto.PasswordLoginChallengeResponse;
import com.github.thundax.bacon.auth.interfaces.dto.PasswordLoginRequest;
import com.github.thundax.bacon.auth.interfaces.dto.SmsLoginRequest;
import com.github.thundax.bacon.auth.interfaces.dto.TokenRefreshRequest;
import com.github.thundax.bacon.auth.interfaces.dto.WecomLoginRequest;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@WrappedApiController
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
    public UserLoginDTO passwordLogin(@Valid @RequestBody PasswordLoginRequest request) {
        return loginApplicationService.loginByPassword(new PasswordLoginCommand(request.getTenantId(), request.getAccount(),
                request.getPassword(), request.getRsaKeyId(), request.getCaptchaKey(), request.getCaptchaCode()));
    }

    @Operation(summary = "短信登录")
    @PostMapping("/login/sms")
    public UserLoginDTO smsLogin(@Valid @RequestBody SmsLoginRequest request) {
        return loginApplicationService.loginBySms(request.getPhone(), request.getSmsCaptcha());
    }

    @Operation(summary = "企微登录")
    @PostMapping("/login/wecom")
    public UserLoginDTO wecomLogin(@Valid @RequestBody WecomLoginRequest request) {
        return loginApplicationService.loginByWecom(request.getCode());
    }

    @Operation(summary = "GitHub 登录回调")
    @GetMapping("/login/github/callback")
    public UserLoginDTO githubLogin(@RequestParam("code") String code) {
        return loginApplicationService.loginByGithub(code);
    }

    @Operation(summary = "刷新访问令牌")
    @PostMapping("/token/refresh")
    public UserTokenRefreshDTO refresh(@Valid @RequestBody TokenRefreshRequest request) {
        return tokenApplicationService.refresh(request.getRefreshToken());
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public void logout(@RequestHeader("Authorization") String authorization) {
        sessionApplicationService.logout(extractBearerToken(authorization));
    }

    @Operation(summary = "修改当前用户密码")
    @PostMapping("/password/change")
    public void changePassword(@RequestHeader("Authorization") String authorization,
                               @Valid @RequestBody PasswordChangeRequest request) {
        passwordApplicationService.changePassword(extractBearerToken(authorization),
                request.getOldPassword(), request.getNewPassword());
    }

    @Operation(summary = "获取当前会话信息")
    @GetMapping("/session/current")
    public CurrentSessionDTO currentSession(@RequestHeader("Authorization") String authorization) {
        return sessionApplicationService.currentSession(extractBearerToken(authorization));
    }

    private String extractBearerToken(String authorization) {
        return authorization != null && authorization.startsWith("Bearer ")
                ? authorization.substring("Bearer ".length())
                : authorization;
    }
}
