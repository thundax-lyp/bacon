package com.github.thundax.bacon.auth.domain.model.entity;

import com.github.thundax.bacon.auth.domain.model.valueobject.ClientId;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * OAuth2 刷新令牌领域实体。
 */
@Getter
@AllArgsConstructor
public class OAuthRefreshToken {

    /** 令牌主键。 */
    private final String tokenId;
    /** 令牌哈希。 */
    private final String tokenHash;
    /** 关联访问令牌主键。 */
    private final String accessTokenId;
    /** 客户端标识。 */
    private final ClientId clientId;
    /** 所属租户编号。 */
    private final TenantId tenantId;
    /** 用户主键。 */
    private final UserId userId;
    /** 签发时间。 */
    private final Instant issuedAt;
    /** 过期时间。 */
    private final Instant expireAt;
    /** 令牌状态。 */
    private String tokenStatus;

    public OAuthRefreshToken(String tokenId, String tokenHash, String accessTokenId, String clientId, Long tenantId,
                             Long userId, Instant issuedAt, Instant expireAt) {
        this(tokenId, tokenHash, accessTokenId,
                clientId == null ? null : ClientId.of(clientId),
                tenantId == null ? null : TenantId.of(tenantId),
                userId == null ? null : UserId.of(userId),
                issuedAt, expireAt, "ACTIVE");
    }

    public String getClientIdValue() {
        return clientId == null ? null : clientId.value();
    }

    public Long getTenantIdValue() {
        return tenantId == null ? null : tenantId.value();
    }

    public Long getUserIdValue() {
        return userId == null ? null : userId.value();
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
