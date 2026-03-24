package com.github.thundax.bacon.auth.infra.repository.impl;

import com.github.thundax.bacon.auth.domain.entity.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryAuthStore {

    private final Map<String, AuthSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, RefreshTokenSession> refreshTokenSessions = new ConcurrentHashMap<>();
    private final Map<String, OAuthClient> clients = new ConcurrentHashMap<>();
    private final Map<String, OAuthAuthorizationRequest> authorizationRequests = new ConcurrentHashMap<>();
    private final Map<String, OAuthAuthorizationRequest> authorizationCodes = new ConcurrentHashMap<>();
    private final Map<String, OAuthAccessToken> accessTokens = new ConcurrentHashMap<>();
    private final Map<String, OAuthRefreshToken> oauthRefreshTokens = new ConcurrentHashMap<>();

    public InMemoryAuthStore() {
        Instant now = Instant.now();
        OAuthClient demoClient = new OAuthClient(1L, "demo-client", "demo-secret", "Demo OAuth Client",
                "CONFIDENTIAL", Set.of("authorization_code", "refresh_token"), Set.of("openid", "profile"),
                Set.of(
                        "http://127.0.0.1:3000/callback",
                        "http://127.0.0.1:8080/api/swagger-ui/oauth2-redirect.html"
                ), 1800L, 2592000L, true,
                "dev@bacon.local", "demo", now, now);
        clients.put(demoClient.getClientId(), demoClient);
    }

    public Map<String, AuthSession> getSessions() {
        return sessions;
    }

    public Map<String, RefreshTokenSession> getRefreshTokenSessions() {
        return refreshTokenSessions;
    }

    public Map<String, OAuthClient> getClients() {
        return clients;
    }

    public Map<String, OAuthAuthorizationRequest> getAuthorizationRequests() {
        return authorizationRequests;
    }

    public Map<String, OAuthAuthorizationRequest> getAuthorizationCodes() {
        return authorizationCodes;
    }

    public Map<String, OAuthAccessToken> getAccessTokens() {
        return accessTokens;
    }

    public Map<String, OAuthRefreshToken> getOauthRefreshTokens() {
        return oauthRefreshTokens;
    }
}
