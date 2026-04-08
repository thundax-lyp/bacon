package com.github.thundax.bacon.auth.infra.repository.impl;

import com.github.thundax.bacon.auth.domain.model.entity.OAuthAccessToken;
import com.github.thundax.bacon.auth.domain.model.entity.OAuthAuthorizationRequest;
import com.github.thundax.bacon.auth.domain.model.entity.OAuthRefreshToken;
import com.github.thundax.bacon.auth.domain.repository.OAuthAuthorizationRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!test")
public class RedisOAuthAuthorizationRepositoryImpl implements OAuthAuthorizationRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisOAuthAuthorizationRepositoryImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public OAuthAuthorizationRequest saveAuthorizationRequest(OAuthAuthorizationRequest authorizationRequest) {
        redisTemplate.opsForValue().set(AuthRedisKeyHelper.authorizationRequest(authorizationRequest.getAuthorizationRequestId()),
                AuthorizationRequestSnapshot.fromDomain(authorizationRequest), ttl(authorizationRequest.getExpireAt()));
        return authorizationRequest;
    }

    @Override
    public Optional<OAuthAuthorizationRequest> findAuthorizationRequestById(String authorizationRequestId) {
        return readValue(AuthRedisKeyHelper.authorizationRequest(authorizationRequestId), AuthorizationRequestSnapshot.class)
                .map(AuthorizationRequestSnapshot::toDomain);
    }

    @Override
    public void saveAuthorizationCode(String authorizationCode, OAuthAuthorizationRequest authorizationRequest) {
        redisTemplate.opsForValue().set(AuthRedisKeyHelper.authorizationCode(authorizationCode),
                AuthorizationRequestSnapshot.fromDomain(authorizationRequest), ttl(authorizationRequest.getExpireAt()));
    }

    @Override
    public Optional<OAuthAuthorizationRequest> findAuthorizationRequestByCode(String authorizationCode) {
        return readValue(AuthRedisKeyHelper.authorizationCode(authorizationCode), AuthorizationRequestSnapshot.class)
                .map(AuthorizationRequestSnapshot::toDomain);
    }

    @Override
    public OAuthAccessToken saveAccessToken(OAuthAccessToken accessToken) {
        redisTemplate.opsForValue().set(AuthRedisKeyHelper.accessToken(accessToken.getTokenHash()),
                AccessTokenSnapshot.fromDomain(accessToken),
                ttl(accessToken.getExpireAt()));
        return accessToken;
    }

    @Override
    public Optional<OAuthAccessToken> findAccessTokenByHash(String tokenHash) {
        return readValue(AuthRedisKeyHelper.accessToken(tokenHash), AccessTokenSnapshot.class)
                .map(AccessTokenSnapshot::toDomain);
    }

    @Override
    public OAuthRefreshToken saveOAuthRefreshToken(OAuthRefreshToken refreshToken) {
        redisTemplate.opsForValue().set(AuthRedisKeyHelper.refreshOAuthToken(refreshToken.getTokenHash()),
                RefreshTokenSnapshot.fromDomain(refreshToken),
                ttl(refreshToken.getExpireAt()));
        return refreshToken;
    }

    @Override
    public Optional<OAuthRefreshToken> findOAuthRefreshTokenByHash(String tokenHash) {
        return readValue(AuthRedisKeyHelper.refreshOAuthToken(tokenHash), RefreshTokenSnapshot.class)
                .map(RefreshTokenSnapshot::toDomain);
    }

    private <T> Optional<T> readValue(String key, Class<T> type) {
        Object value = redisTemplate.opsForValue().get(key);
        return type.isInstance(value) ? Optional.of(type.cast(value)) : Optional.empty();
    }

    private Duration ttl(Instant expireAt) {
        long ttlSeconds = Math.max(Duration.between(Instant.now(), expireAt).getSeconds(), 1L);
        return Duration.ofSeconds(ttlSeconds);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class AuthorizationRequestSnapshot {
        private String authorizationRequestId;
        private String clientId;
        private String redirectUri;
        private Set<String> scopes;
        private String state;
        private String codeChallenge;
        private String codeChallengeMethod;
        private Long tenantId;
        private Long userId;
        private Instant expireAt;
        private boolean used;

        private static AuthorizationRequestSnapshot fromDomain(OAuthAuthorizationRequest authorizationRequest) {
            return new AuthorizationRequestSnapshot(authorizationRequest.getAuthorizationRequestId(),
                    authorizationRequest.getClientId(), authorizationRequest.getRedirectUri(), authorizationRequest.getScopes(),
                    authorizationRequest.getState(), authorizationRequest.getCodeChallenge(),
                    authorizationRequest.getCodeChallengeMethod(), authorizationRequest.getTenantId(),
                    authorizationRequest.getUserId(), authorizationRequest.getExpireAt(), authorizationRequest.isUsed());
        }

        private OAuthAuthorizationRequest toDomain() {
            OAuthAuthorizationRequest request = new OAuthAuthorizationRequest(authorizationRequestId, clientId, redirectUri,
                    scopes == null ? null : scopes.stream().toList(), state, codeChallenge, codeChallengeMethod,
                    tenantId, userId, expireAt);
            if (used) {
                request.markUsed();
            }
            return request;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class AccessTokenSnapshot {
        private String tokenId;
        private String tokenHash;
        private String clientId;
        private Long tenantId;
        private Long userId;
        private Set<String> scopes;
        private Instant issuedAt;
        private Instant expireAt;
        private String tokenStatus;

        private static AccessTokenSnapshot fromDomain(OAuthAccessToken accessToken) {
            return new AccessTokenSnapshot(accessToken.getTokenId(), accessToken.getTokenHash(), accessToken.getClientId(),
                    accessToken.getTenantId(), accessToken.getUserId(), accessToken.getScopes(),
                    accessToken.getIssuedAt(), accessToken.getExpireAt(), accessToken.getTokenStatus());
        }

        private OAuthAccessToken toDomain() {
            OAuthAccessToken accessToken = new OAuthAccessToken(tokenId, tokenHash, clientId, tenantId, userId,
                    scopes == null ? null : scopes.stream().toList(), issuedAt, expireAt);
            if ("REVOKED".equals(tokenStatus)) {
                accessToken.revoke();
            }
            return accessToken;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class RefreshTokenSnapshot {
        private String tokenId;
        private String tokenHash;
        private String accessTokenId;
        private String clientId;
        private Long tenantId;
        private Long userId;
        private Instant issuedAt;
        private Instant expireAt;
        private String tokenStatus;

        private static RefreshTokenSnapshot fromDomain(OAuthRefreshToken refreshToken) {
            return new RefreshTokenSnapshot(refreshToken.getTokenId(), refreshToken.getTokenHash(),
                    refreshToken.getAccessTokenId(), refreshToken.getClientIdValue(), refreshToken.getTenantIdValue(),
                    refreshToken.getUserIdValue(), refreshToken.getIssuedAt(), refreshToken.getExpireAt(),
                    refreshToken.getTokenStatus());
        }

        private OAuthRefreshToken toDomain() {
            OAuthRefreshToken refreshToken = new OAuthRefreshToken(tokenId, tokenHash, accessTokenId, clientId, tenantId,
                    userId, issuedAt, expireAt);
            if ("USED".equals(tokenStatus)) {
                refreshToken.markUsed();
            } else if ("REVOKED".equals(tokenStatus)) {
                refreshToken.revoke();
            }
            return refreshToken;
        }
    }
}
