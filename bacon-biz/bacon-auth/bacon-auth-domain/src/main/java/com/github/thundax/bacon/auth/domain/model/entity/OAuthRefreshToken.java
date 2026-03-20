package com.github.thundax.bacon.auth.domain.model.entity;

import java.time.Instant;

public class OAuthRefreshToken {

    private final String tokenId;
    private final String tokenHash;
    private final String accessTokenId;
    private final String clientId;
    private final Long tenantId;
    private final Long userId;
    private final Instant issuedAt;
    private final Instant expireAt;
    private String tokenStatus;

    public OAuthRefreshToken(String tokenId, String tokenHash, String accessTokenId, String clientId, Long tenantId,
                             Long userId, Instant issuedAt, Instant expireAt) {
        this.tokenId = tokenId;
        this.tokenHash = tokenHash;
        this.accessTokenId = accessTokenId;
        this.clientId = clientId;
        this.tenantId = tenantId;
        this.userId = userId;
        this.issuedAt = issuedAt;
        this.expireAt = expireAt;
        this.tokenStatus = "ACTIVE";
    }

    public void markUsed() {
        this.tokenStatus = "USED";
    }

    public void revoke() {
        this.tokenStatus = "REVOKED";
    }

    public String getTokenId() {
        return tokenId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public String getAccessTokenId() {
        return accessTokenId;
    }

    public String getClientId() {
        return clientId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public Long getUserId() {
        return userId;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public Instant getExpireAt() {
        return expireAt;
    }

    public String getTokenStatus() {
        return tokenStatus;
    }
}
