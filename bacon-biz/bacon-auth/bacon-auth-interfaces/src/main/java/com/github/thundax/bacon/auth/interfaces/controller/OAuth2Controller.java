package com.github.thundax.bacon.auth.interfaces.controller;

import com.github.thundax.bacon.auth.application.command.OAuth2AuthorizationCommandApplicationService;
import com.github.thundax.bacon.auth.application.command.OAuth2AuthorizeCommand;
import com.github.thundax.bacon.auth.application.command.OAuth2DecisionCommand;
import com.github.thundax.bacon.auth.application.command.OAuth2RevokeCommand;
import com.github.thundax.bacon.auth.application.command.OAuth2TokenCommand;
import com.github.thundax.bacon.auth.application.query.OAuth2AuthorizationQueryApplicationService;
import com.github.thundax.bacon.auth.application.query.OAuth2IntrospectionQuery;
import com.github.thundax.bacon.auth.application.query.OAuth2UserinfoQuery;
import com.github.thundax.bacon.auth.interfaces.request.OAuth2DecisionRequest;
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
import jakarta.validation.constraints.NotBlank;
import org.springframework.util.MultiValueMap;
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
@RequestMapping("/auth/oauth2")
@Validated
@Tag(name = "Auth-OAuth2", description = "OAuth2 授权协议接口")
public class OAuth2Controller {

    private final OAuth2AuthorizationCommandApplicationService oAuth2AuthorizationCommandApplicationService;
    private final OAuth2AuthorizationQueryApplicationService oAuth2AuthorizationQueryApplicationService;

    public OAuth2Controller(
            OAuth2AuthorizationCommandApplicationService oAuth2AuthorizationCommandApplicationService,
            OAuth2AuthorizationQueryApplicationService oAuth2AuthorizationQueryApplicationService) {
        this.oAuth2AuthorizationCommandApplicationService = oAuth2AuthorizationCommandApplicationService;
        this.oAuth2AuthorizationQueryApplicationService = oAuth2AuthorizationQueryApplicationService;
    }

    @Operation(summary = "发起 OAuth2 授权")
    @GetMapping("/authorize")
    public OAuth2AuthorizationViewResponse authorize(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam("client_id") @NotBlank(message = "client_id must not be blank") String clientId,
            @RequestParam("redirect_uri") @NotBlank(message = "redirect_uri must not be blank") String redirectUri,
            @RequestParam(value = "scope", required = false) String scope,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "code_challenge", required = false) String codeChallenge,
            @RequestParam(value = "code_challenge_method", required = false) String codeChallengeMethod) {
        return OAuth2AuthorizationViewResponse.from(oAuth2AuthorizationCommandApplicationService.authorize(
                new OAuth2AuthorizeCommand(
                        BearerTokenUtils.extractToken(authorization),
                        clientId,
                        redirectUri,
                        scope,
                        state,
                        codeChallenge,
                        codeChallengeMethod)));
    }

    @Operation(summary = "提交 OAuth2 授权决策")
    @PostMapping("/authorize-decision")
    public OAuth2AuthorizationDecisionResponse decide(@Valid @RequestBody OAuth2DecisionRequest request) {
        return OAuth2AuthorizationDecisionResponse.from(
                oAuth2AuthorizationCommandApplicationService.decide(
                        new OAuth2DecisionCommand(request.getAuthorizationRequestId(), request.getDecision())));
    }

    @Operation(summary = "换取 OAuth2 访问令牌")
    @PostMapping("/tokens")
    public OAuth2TokenResponse token(@RequestParam MultiValueMap<String, String> request) {
        return OAuth2TokenResponse.from(oAuth2AuthorizationCommandApplicationService.token(new OAuth2TokenCommand(
                request.getFirst("grant_type"),
                request.getFirst("code"),
                request.getFirst("redirect_uri"),
                request.getFirst("client_id"),
                request.getFirst("client_secret"),
                request.getFirst("code_verifier"),
                request.getFirst("refresh_token"))));
    }

    @Operation(summary = "校验 OAuth2 令牌")
    @PostMapping("/introspections")
    public OAuth2IntrospectionResponse introspect(
            @RequestParam("token") @NotBlank(message = "token must not be blank") String token,
            @RequestParam("client_id") @NotBlank(message = "client_id must not be blank") String clientId,
            @RequestParam("client_secret") @NotBlank(message = "client_secret must not be blank") String clientSecret) {
        return OAuth2IntrospectionResponse.from(
                oAuth2AuthorizationQueryApplicationService.introspect(
                        new OAuth2IntrospectionQuery(token, clientId, clientSecret)));
    }

    @Operation(summary = "撤销 OAuth2 令牌")
    @PostMapping("/revocations")
    public void revoke(
            @RequestParam("token") @NotBlank(message = "token must not be blank") String token,
            @RequestParam("client_id") @NotBlank(message = "client_id must not be blank") String clientId,
            @RequestParam("client_secret") @NotBlank(message = "client_secret must not be blank") String clientSecret) {
        oAuth2AuthorizationCommandApplicationService.revoke(new OAuth2RevokeCommand(token, clientId, clientSecret));
    }

    @Operation(summary = "获取 OAuth2 用户信息")
    @GetMapping("/userinfo")
    public OAuth2UserinfoResponse userinfo(
            @RequestHeader("Authorization") @NotBlank(message = "Authorization header must not be blank")
                    String authorization) {
        return OAuth2UserinfoResponse.from(
                oAuth2AuthorizationQueryApplicationService.userinfo(
                        new OAuth2UserinfoQuery(BearerTokenUtils.extractToken(authorization))));
    }
}
