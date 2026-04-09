package com.github.thundax.bacon.auth.interfaces.controller;

import com.github.thundax.bacon.auth.application.command.OAuth2AuthorizationApplicationService;
import com.github.thundax.bacon.auth.interfaces.dto.OAuth2DecisionRequest;
import com.github.thundax.bacon.auth.interfaces.response.OAuth2AuthorizationDecisionResponse;
import com.github.thundax.bacon.auth.interfaces.response.OAuth2AuthorizationViewResponse;
import com.github.thundax.bacon.auth.interfaces.response.OAuth2IntrospectionResponse;
import com.github.thundax.bacon.auth.interfaces.response.OAuth2TokenResponse;
import com.github.thundax.bacon.auth.interfaces.response.OAuth2UserinfoResponse;
import com.github.thundax.bacon.common.web.annotation.WrappedApiController;
import com.github.thundax.bacon.common.web.util.BearerTokenUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@WrappedApiController
@RequestMapping("/oauth2")
@Tag(name = "Auth-OAuth2", description = "OAuth2 授权协议接口")
public class OAuth2Controller {

    private final OAuth2AuthorizationApplicationService oAuth2AuthorizationApplicationService;

    public OAuth2Controller(OAuth2AuthorizationApplicationService oAuth2AuthorizationApplicationService) {
        this.oAuth2AuthorizationApplicationService = oAuth2AuthorizationApplicationService;
    }

    @Operation(summary = "发起 OAuth2 授权")
    @GetMapping("/authorize")
    public OAuth2AuthorizationViewResponse authorize(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("client_id") String clientId,
            @RequestParam("redirect_uri") String redirectUri,
            @RequestParam(value = "scope", required = false) String scope,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "code_challenge", required = false) String codeChallenge,
            @RequestParam(value = "code_challenge_method", required = false) String codeChallengeMethod) {
        return OAuth2AuthorizationViewResponse.from(oAuth2AuthorizationApplicationService.authorize(
                BearerTokenUtils.extractToken(authorization),
                clientId,
                redirectUri,
                scope,
                state,
                codeChallenge,
                codeChallengeMethod));
    }

    @Operation(summary = "提交 OAuth2 授权决策")
    @PostMapping("/authorize/decision")
    public OAuth2AuthorizationDecisionResponse decide(@Valid @RequestBody OAuth2DecisionRequest request) {
        return OAuth2AuthorizationDecisionResponse.from(oAuth2AuthorizationApplicationService.decide(
                request.getAuthorizationRequestId(), request.getDecision()));
    }

    @Operation(summary = "换取 OAuth2 访问令牌")
    @PostMapping("/token")
    public OAuth2TokenResponse token(@RequestParam MultiValueMap<String, String> request) {
        return OAuth2TokenResponse.from(oAuth2AuthorizationApplicationService.token(
                request.getFirst("grant_type"),
                request.getFirst("code"),
                request.getFirst("redirect_uri"),
                request.getFirst("client_id"),
                request.getFirst("client_secret"),
                request.getFirst("code_verifier"),
                request.getFirst("refresh_token")));
    }

    @Operation(summary = "校验 OAuth2 令牌")
    @PostMapping("/introspect")
    public OAuth2IntrospectionResponse introspect(
            @RequestParam("token") String token,
            @RequestParam("client_id") String clientId,
            @RequestParam("client_secret") String clientSecret) {
        return OAuth2IntrospectionResponse.from(
                oAuth2AuthorizationApplicationService.introspect(token, clientId, clientSecret));
    }

    @Operation(summary = "撤销 OAuth2 令牌")
    @PostMapping("/revoke")
    public void revoke(
            @RequestParam("token") String token,
            @RequestParam("client_id") String clientId,
            @RequestParam("client_secret") String clientSecret) {
        oAuth2AuthorizationApplicationService.revoke(token, clientId, clientSecret);
    }

    @Operation(summary = "获取 OAuth2 用户信息")
    @GetMapping("/userinfo")
    public OAuth2UserinfoResponse userinfo(@RequestHeader("Authorization") String authorization) {
        return OAuth2UserinfoResponse.from(
                oAuth2AuthorizationApplicationService.userinfo(BearerTokenUtils.extractToken(authorization)));
    }
}
