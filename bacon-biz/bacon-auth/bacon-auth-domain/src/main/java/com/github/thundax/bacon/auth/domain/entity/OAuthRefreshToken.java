package com.github.thundax.bacon.auth.domain.entity;

import lombok.Getter;

import java.time.Instant;

@Getter
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
}
