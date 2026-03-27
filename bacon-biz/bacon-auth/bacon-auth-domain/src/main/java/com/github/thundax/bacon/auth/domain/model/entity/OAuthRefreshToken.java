package com.github.thundax.bacon.auth.domain.model.entity;

import lombok.Getter;

import java.time.Instant;

/**
 * OAuth2 刷新令牌领域实体。
 */
@Getter
public class OAuthRefreshToken {

    /** 令牌主键。 */
    private final String tokenId;
    /** 令牌哈希。 */
    private final String tokenHash;
    /** 关联访问令牌主键。 */
    private final String accessTokenId;
    /** 客户端标识。 */
    private final String clientId;
    /** 所属租户主键。 */
    private final Long tenantId;
    /** 用户主键。 */
    private final Long userId;
    /** 签发时间。 */
    private final Instant issuedAt;
    /** 过期时间。 */
    private final Instant expireAt;
    /** 令牌状态。 */
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
