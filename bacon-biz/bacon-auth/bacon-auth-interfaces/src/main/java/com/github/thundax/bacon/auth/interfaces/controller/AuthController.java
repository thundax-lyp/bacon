package com.github.thundax.bacon.auth.interfaces.controller;

import com.github.thundax.bacon.auth.api.dto.CurrentSessionResponse;
import com.github.thundax.bacon.auth.api.dto.UserLoginResponse;
import com.github.thundax.bacon.auth.api.dto.UserTokenRefreshResponse;
import com.github.thundax.bacon.auth.application.service.LoginApplicationService;
import com.github.thundax.bacon.auth.application.service.PasswordApplicationService;
import com.github.thundax.bacon.auth.application.service.SessionApplicationService;
import com.github.thundax.bacon.auth.application.service.TokenApplicationService;
import com.github.thundax.bacon.auth.interfaces.dto.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
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

    @PostMapping("/login/password")
    public UserLoginResponse passwordLogin(@RequestBody PasswordLoginRequest request) {
        return loginApplicationService.loginByPassword(request.getAccount(), request.getPassword());
    }

    @PostMapping("/login/sms")
    public UserLoginResponse smsLogin(@RequestBody SmsLoginRequest request) {
        return loginApplicationService.loginBySms(request.getPhone(), request.getSmsCaptcha());
    }

    @PostMapping("/login/wecom")
    public UserLoginResponse wecomLogin(@RequestBody WecomLoginRequest request) {
        return loginApplicationService.loginByWecom(request.getCode());
    }

    @GetMapping("/login/github/callback")
    public UserLoginResponse githubLogin(@RequestParam("code") String code) {
        return loginApplicationService.loginByGithub(code);
    }

    @PostMapping("/token/refresh")
    public UserTokenRefreshResponse refresh(@RequestBody TokenRefreshRequest request) {
        return tokenApplicationService.refresh(request.getRefreshToken());
    }

    @PostMapping("/logout")
    public void logout(@RequestHeader("Authorization") String authorization) {
        sessionApplicationService.logout(extractBearerToken(authorization));
    }

    @PostMapping("/password/change")
    public void changePassword(@RequestHeader("Authorization") String authorization,
                               @RequestBody PasswordChangeRequest request) {
        passwordApplicationService.changePassword(extractBearerToken(authorization),
                request.getOldPassword(), request.getNewPassword());
    }

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
