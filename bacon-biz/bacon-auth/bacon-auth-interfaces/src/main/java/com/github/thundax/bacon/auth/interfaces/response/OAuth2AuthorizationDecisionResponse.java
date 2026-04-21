package com.github.thundax.bacon.auth.interfaces.response;

import com.github.thundax.bacon.auth.application.command.OAuth2AuthorizationCommandApplicationService.AuthorizationDecisionResult;

/**
 * OAuth2 授权确认结果响应对象。
 */
public record OAuth2AuthorizationDecisionResponse(
        /** 重定向 URI。 */
        String redirectUri,
        /** 授权码。 */
        String authorizationCode) {

    public static OAuth2AuthorizationDecisionResponse from(AuthorizationDecisionResult result) {
        return new OAuth2AuthorizationDecisionResponse(result.getRedirectUri(), result.getAuthorizationCode());
    }
}
