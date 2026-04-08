package com.github.thundax.bacon.auth.domain.model.entity;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * OAuth2 访问令牌领域实体。
 */
@Getter
@AllArgsConstructor
public class OAuthAccessToken {

    /** 令牌主键。 */
    private final String tokenId;
    /** 令牌哈希。 */
    private final String tokenHash;
    /** 客户端标识。 */
    private final String clientId;
    /** 所属租户编号。 */
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
                            List<String> scopes, Instant issuedAt, Instant expireAt) {
        this(tokenId, tokenHash, clientId, tenantId, userId, toLinkedHashSet(scopes), issuedAt, expireAt, "ACTIVE");
    }

    private static Set<String> toLinkedHashSet(List<String> values) {
        return values == null ? new LinkedHashSet<>() : new LinkedHashSet<>(values);
    }

    public void revoke() {
        // 访问令牌只保留撤销语义；鉴权链路据此拒绝已签发但被强制作废的 token。
        this.tokenStatus = "REVOKED";
    }
}
