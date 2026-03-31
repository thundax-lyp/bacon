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
    /** 所属租户编号。 */
    private final String tenantNo;
    /** 用户主键。 */
    private final Long userId;
    /** 签发时间。 */
    private final Instant issuedAt;
    /** 过期时间。 */
    private final Instant expireAt;
    /** 令牌状态。 */
    private String tokenStatus;

    public OAuthRefreshToken(String tokenId, String tokenHash, String accessTokenId, String clientId, String tenantNo,
                             Long userId, Instant issuedAt, Instant expireAt) {
        this.tokenId = tokenId;
        this.tokenHash = tokenHash;
        this.accessTokenId = accessTokenId;
        this.clientId = clientId;
        this.tenantNo = tenantNo;
        this.userId = userId;
        this.issuedAt = issuedAt;
        this.expireAt = expireAt;
        this.tokenStatus = "ACTIVE";
    }

    public void markUsed() {
        // 刷新令牌采用一次性轮转；被成功消费后必须立刻标记 USED，禁止再次换发访问令牌。
        this.tokenStatus = "USED";
    }

    public void revoke() {
        // 主动撤销用于登出或风控失效，和 USED 区分开，便于审计令牌为什么不可再用。
        this.tokenStatus = "REVOKED";
    }
}
