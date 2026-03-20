package com.github.thundax.bacon.auth.domain.entity;

import java.time.Instant;
import java.util.Set;
import lombok.Getter;

@Getter
public class OAuthAccessToken {

    private final String tokenId;
    private final String tokenHash;
    private final String clientId;
    private final Long tenantId;
    private final Long userId;
    private final Set<String> scopes;
    private final Instant issuedAt;
    private final Instant expireAt;
    private String tokenStatus;

    public OAuthAccessToken(String tokenId, String tokenHash, String clientId, Long tenantId, Long userId, Set<String> scopes,
                            Instant issuedAt, Instant expireAt) {
        this.tokenId = tokenId;
        this.tokenHash = tokenHash;
        this.clientId = clientId;
        this.tenantId = tenantId;
        this.userId = userId;
        this.scopes = scopes;
        this.issuedAt = issuedAt;
        this.expireAt = expireAt;
        this.tokenStatus = "ACTIVE";
    }

    public void revoke() {
        this.tokenStatus = "REVOKED";
    }
}
