package com.github.thundax.bacon.auth.domain.entity;

import lombok.Getter;

import java.time.Instant;
import java.util.Set;

@Getter
public class OAuthAuthorizationRequest {

    private final String authorizationRequestId;
    private final String clientId;
    private final String redirectUri;
    private final Set<String> scopes;
    private final String state;
    private final String codeChallenge;
    private final String codeChallengeMethod;
    private final Long tenantId;
    private final Long userId;
    private final Instant expireAt;
    private boolean used;

    public OAuthAuthorizationRequest(String authorizationRequestId, String clientId, String redirectUri, Set<String> scopes,
                                     String state, String codeChallenge, String codeChallengeMethod, Long tenantId,
                                     Long userId, Instant expireAt) {
        this.authorizationRequestId = authorizationRequestId;
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        this.scopes = scopes;
        this.state = state;
        this.codeChallenge = codeChallenge;
        this.codeChallengeMethod = codeChallengeMethod;
        this.tenantId = tenantId;
        this.userId = userId;
        this.expireAt = expireAt;
    }

    public void markUsed() {
        this.used = true;
    }
}
