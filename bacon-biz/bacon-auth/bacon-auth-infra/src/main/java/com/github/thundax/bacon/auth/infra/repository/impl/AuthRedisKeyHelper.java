package com.github.thundax.bacon.auth.infra.repository.impl;

final class AuthRedisKeyHelper {

    private AuthRedisKeyHelper() {
    }

    static String session(String sessionId) {
        return "auth:session:" + sessionId;
    }

    static String refreshToken(String refreshTokenHash) {
        return "auth:refresh-token:" + refreshTokenHash;
    }

    static String userSessions(String tenantNo, String userId) {
        return "auth:user-sessions:" + tenantNo + ":" + userId;
    }

    static String tenantSessions(String tenantNo) {
        return "auth:tenant-sessions:" + tenantNo;
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
