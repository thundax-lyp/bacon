package com.github.thundax.bacon.auth.interfaces.response;

import com.github.thundax.bacon.auth.application.command.OAuth2AuthorizationApplicationService.AuthorizationView;

public record OAuth2AuthorizationViewResponse(String authorizationRequestId, String clientId, String clientName,
                                              String scope, String state) {

    public static OAuth2AuthorizationViewResponse from(AuthorizationView view) {
        return new OAuth2AuthorizationViewResponse(view.getAuthorizationRequestId(), view.getClientId(),
                view.getClientName(), view.getScope(), view.getState());
    }
}
