package com.github.thundax.bacon.auth.domain.model.entity;

import lombok.Getter;

import java.time.Instant;
import java.util.Set;

/**
 * OAuth2 授权请求领域实体。
 */
@Getter
public class OAuthAuthorizationRequest {

    /** 授权请求标识。 */
    private final String authorizationRequestId;
    /** 客户端标识。 */
    private final String clientId;
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
    private final String tenantNo;
    /** 用户主键。 */
    private final String userId;
    /** 过期时间。 */
    private final Instant expireAt;
    /** 是否已使用。 */
    private boolean used;

    public OAuthAuthorizationRequest(String authorizationRequestId, String clientId, String redirectUri,
                                     Set<String> scopes, String state, String codeChallenge,
                                     String codeChallengeMethod, String tenantNo, String userId, Instant expireAt) {
        this.authorizationRequestId = authorizationRequestId;
        this.clientId = clientId;
        this.redirectUri = redirectUri;
        this.scopes = scopes;
        this.state = state;
        this.codeChallenge = codeChallenge;
        this.codeChallengeMethod = codeChallengeMethod;
        this.tenantNo = tenantNo;
        this.userId = userId;
        this.expireAt = expireAt;
    }

    public void markUsed() {
        // 授权请求只能消费一次；上层在换取 code 后立即标记，避免重放同一批准结果。
        this.used = true;
    }
}
