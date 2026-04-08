package com.github.thundax.bacon.auth.domain.model.entity;

import com.github.thundax.bacon.auth.domain.model.enums.AccessTokenStatus;
import com.github.thundax.bacon.auth.domain.model.valueobject.ClientCode;
import com.github.thundax.bacon.common.id.domain.TenantId;
import com.github.thundax.bacon.common.id.domain.UserId;
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
    private final ClientCode clientId;
    /** 所属租户编号。 */
    private final TenantId tenantId;
    /** 用户主键。 */
    private final UserId userId;
    /** 授权范围集合。 */
    private final Set<String> scopes;
    /** 签发时间。 */
    private final Instant issuedAt;
    /** 过期时间。 */
    private final Instant expireAt;
    /** 令牌状态。 */
    private AccessTokenStatus status;

    public OAuthAccessToken(String tokenId, String tokenHash, String clientId, Long tenantId, Long userId,
                            List<String> scopes, Instant issuedAt, Instant expireAt) {
        this(tokenId, tokenHash,
                clientId == null ? null : ClientCode.of(clientId),
                tenantId == null ? null : TenantId.of(tenantId),
                userId == null ? null : UserId.of(userId),
                toLinkedHashSet(scopes), issuedAt, expireAt, AccessTokenStatus.ACTIVE);
    }

    private static Set<String> toLinkedHashSet(List<String> values) {
        return values == null ? new LinkedHashSet<>() : new LinkedHashSet<>(values);
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

    public String getStatusValue() {
        return status == null ? null : status.value();
    }

    public boolean isActive() {
        return AccessTokenStatus.ACTIVE == status;
    }

    public void revoke() {
        // 访问令牌只保留撤销语义；鉴权链路据此拒绝已签发但被强制作废的 token。
        this.status = AccessTokenStatus.REVOKED;
    }
}
