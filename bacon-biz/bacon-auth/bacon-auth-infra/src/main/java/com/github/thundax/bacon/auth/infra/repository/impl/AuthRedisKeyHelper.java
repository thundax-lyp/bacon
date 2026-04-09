package com.github.thundax.bacon.auth.infra.repository.impl;

final class AuthRedisKeyHelper {

    private AuthRedisKeyHelper() {}

    static String session(String sessionId) {
        return "auth:session:" + sessionId;
    }

    static String refreshToken(String refreshTokenHash) {
        return "auth:refresh-token:" + refreshTokenHash;
    }

    static String userSessions(Long tenantId, Long userId) {
        return "auth:user-sessions:" + tenantId + ":" + userId;
    }

    static String tenantSessions(Long tenantId) {
        return "auth:tenant-sessions:" + tenantId;
    }

    static String sessionRefreshTokens(String sessionId) {
        return "auth:session-refresh-tokens:" + sessionId;
    }

    static String authorizationRequest(String authorizationRequestId) {
        return "auth:oauth-authorization-request:" + authorizationRequestId;
    }

    static String authorizationCode(String authorizationCode) {
        return "auth:oauth-code:" + authorizationCode;
    }

    static String accessToken(String tokenHash) {
        return "auth:oauth-access-token:" + tokenHash;
    }

    static String refreshOAuthToken(String tokenHash) {
        return "auth:oauth-refresh-token:" + tokenHash;
    }
}
