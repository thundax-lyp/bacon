package com.github.thundax.bacon.auth.interfaces.controller;

import com.github.thundax.bacon.auth.api.dto.CurrentSessionResponse;
import com.github.thundax.bacon.auth.api.dto.UserLoginResponse;
import com.github.thundax.bacon.auth.api.dto.UserTokenRefreshResponse;
import com.github.thundax.bacon.auth.application.service.LoginApplicationService;
import com.github.thundax.bacon.auth.application.service.PasswordApplicationService;
import com.github.thundax.bacon.auth.application.service.SessionApplicationService;
import com.github.thundax.bacon.auth.application.service.TokenApplicationService;
import com.github.thundax.bacon.auth.interfaces.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Auth", description = "认证与会话接口")
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

    @Operation(summary = "账号密码登录")
    @PostMapping("/login/password")
    public UserLoginResponse passwordLogin(@RequestBody PasswordLoginRequest request) {
        return loginApplicationService.loginByPassword(request.getAccount(), request.getPassword());
    }

    @Operation(summary = "短信登录")
    @PostMapping("/login/sms")
    public UserLoginResponse smsLogin(@RequestBody SmsLoginRequest request) {
        return loginApplicationService.loginBySms(request.getPhone(), request.getSmsCaptcha());
    }

    @Operation(summary = "企微登录")
    @PostMapping("/login/wecom")
    public UserLoginResponse wecomLogin(@RequestBody WecomLoginRequest request) {
        return loginApplicationService.loginByWecom(request.getCode());
    }

    @Operation(summary = "GitHub 登录回调")
    @GetMapping("/login/github/callback")
    public UserLoginResponse githubLogin(@RequestParam("code") String code) {
        return loginApplicationService.loginByGithub(code);
    }

    @Operation(summary = "刷新访问令牌")
    @PostMapping("/token/refresh")
    public UserTokenRefreshResponse refresh(@RequestBody TokenRefreshRequest request) {
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
                               @RequestBody PasswordChangeRequest request) {
        passwordApplicationService.changePassword(extractBearerToken(authorization),
                request.getOldPassword(), request.getNewPassword());
    }

    @Operation(summary = "获取当前会话信息")
    @GetMapping("/session/current")
    public CurrentSessionResponse currentSession(@RequestHeader("Authorization") String authorization) {
        return sessionApplicationService.currentSession(extractBearerToken(authorization));
    }

    private String extractBearerToken(String authorization) {
        return authorization != null && authorization.startsWith("Bearer ")
                ? authorization.substring("Bearer ".length())
                : authorization;
    }
}
