package com.github.thundax.bacon.auth.domain.model.entity;

import lombok.Getter;

import java.time.Instant;
import java.util.Set;

/**
 * OAuth2 访问令牌领域实体。
 */
@Getter
public class OAuthAccessToken {

    /** 令牌主键。 */
    private final String tokenId;
    /** 令牌哈希。 */
    private final String tokenHash;
    /** 客户端标识。 */
    private final String clientId;
    /** 所属租户主键。 */
    private final Long tenantId;
    /** 用户主键。 */
    private final Long userId;
    /** 授权范围集合。 */
    private final Set<String> scopes;
    /** 签发时间。 */
    private final Instant issuedAt;
    /** 过期时间。 */
    private final Instant expireAt;
    /** 令牌状态。 */
    private String tokenStatus;

    public OAuthAccessToken(String tokenId, String tokenHash, String clientId, Long tenantId, Long userId,
                            Set<String> scopes, Instant issuedAt, Instant expireAt) {
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
