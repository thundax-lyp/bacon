package com.github.thundax.bacon.auth.interfaces.response;

import com.github.thundax.bacon.auth.application.command.OAuth2AuthorizationApplicationService.AuthorizationView;

/**
 * OAuth2 授权页响应对象。
 */
public record OAuth2AuthorizationViewResponse(
        /** 授权请求标识。 */
        String authorizationRequestId,
        /** 客户端标识。 */
        String clientId,
        /** 客户端名称。 */
        String clientName,
        /** 授权范围。 */
        String scope,
        /** OAuth2 state。 */
        String state) {

    public static OAuth2AuthorizationViewResponse from(AuthorizationView view) {
        return new OAuth2AuthorizationViewResponse(view.getAuthorizationRequestId(), view.getClientId(),
                view.getClientName(), view.getScope(), view.getState());
    }
}
