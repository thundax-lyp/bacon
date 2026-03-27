package com.github.thundax.bacon.auth.interfaces.response;

import com.github.thundax.bacon.auth.application.command.OAuth2AuthorizationApplicationService.AuthorizationDecisionResult;

public record OAuth2AuthorizationDecisionResponse(String redirectUri, String authorizationCode) {

    public static OAuth2AuthorizationDecisionResponse from(AuthorizationDecisionResult result) {
        return new OAuth2AuthorizationDecisionResponse(result.getRedirectUri(), result.getAuthorizationCode());
    }
}
