package com.github.thundax.bacon.auth.domain.model.entity;

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
 * OAuth2 授权请求领域实体。
 */
@Getter
@AllArgsConstructor
public class OAuthAuthorizationRequest {

    /** 授权请求标识。 */
    private final String authorizationRequestId;
    /** 客户端标识。 */
    private final ClientCode clientId;
    /** 重定向 URI。 */
    private final String redirectUri;
    /** 授权范围集合。 */
    private final Set<String> scopes;
    /** OAuth2 state。 */
    private final String state;
    /** PKCE code challenge。 */
    private final String codeChallenge;
    /** PKCE code challenge method。 */
    private final String codeChallengeMethod;
    /** 所属租户编号。 */
    private final TenantId tenantId;
    /** 用户主键。 */
    private final UserId userId;
    /** 过期时间。 */
    private final Instant expireAt;
    /** 是否已使用。 */
    private boolean used;

    public OAuthAuthorizationRequest(String authorizationRequestId, String clientId, String redirectUri,
                                     List<String> scopes, String state, String codeChallenge,
                                     String codeChallengeMethod, Long tenantId, Long userId, Instant expireAt) {
        this(authorizationRequestId,
                clientId == null ? null : ClientCode.of(clientId),
                redirectUri, toLinkedHashSet(scopes), state, codeChallenge,
                codeChallengeMethod,
                tenantId == null ? null : TenantId.of(tenantId),
                userId == null ? null : UserId.of(userId),
                expireAt, false);
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

    public void markUsed() {
        // 授权请求只能消费一次；上层在换取 code 后立即标记，避免重放同一批准结果。
        this.used = true;
    }
}
