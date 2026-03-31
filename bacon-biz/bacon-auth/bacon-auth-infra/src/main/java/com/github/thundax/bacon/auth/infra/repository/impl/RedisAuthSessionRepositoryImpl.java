package com.github.thundax.bacon.auth.infra.repository.impl;

import com.github.thundax.bacon.auth.domain.model.entity.AuthSession;
import com.github.thundax.bacon.auth.domain.model.entity.RefreshTokenSession;
import com.github.thundax.bacon.auth.domain.repository.AuthSessionRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnBean(RedisTemplate.class)
@ConditionalOnMissingBean(AuthSessionRepository.class)
public class RedisAuthSessionRepositoryImpl implements AuthSessionRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisAuthSessionRepositoryImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public AuthSession saveSession(AuthSession authSession) {
        String sessionKey = AuthRedisKeyHelper.session(authSession.getSessionId());
        redisTemplate.opsForValue().set(sessionKey, SessionSnapshot.fromDomain(authSession), ttl(authSession.getExpireAt()));
        redisTemplate.opsForSet().add(AuthRedisKeyHelper.userSessions(authSession.getTenantNo(), authSession.getUserId()),
                authSession.getSessionId());
        redisTemplate.expire(AuthRedisKeyHelper.userSessions(authSession.getTenantNo(), authSession.getUserId()),
                ttl(authSession.getExpireAt()));
        redisTemplate.opsForSet().add(AuthRedisKeyHelper.tenantSessions(authSession.getTenantNo()), authSession.getSessionId());
        redisTemplate.expire(AuthRedisKeyHelper.tenantSessions(authSession.getTenantNo()), ttl(authSession.getExpireAt()));
        return authSession;
    }

    @Override
    public Optional<AuthSession> findSessionBySessionId(String sessionId) {
        return readValue(AuthRedisKeyHelper.session(sessionId), SessionSnapshot.class).map(SessionSnapshot::toDomain);
    }

    @Override
    public List<AuthSession> findSessionsByTenantNoAndUserId(String tenantNo, String userId) {
        return loadSessionsByIndex(AuthRedisKeyHelper.userSessions(tenantNo, userId));
    }

    @Override
    public List<AuthSession> findSessionsByTenantNo(String tenantNo) {
        return loadSessionsByIndex(AuthRedisKeyHelper.tenantSessions(tenantNo));
    }

    @Override
    public RefreshTokenSession saveRefreshToken(RefreshTokenSession refreshTokenSession) {
        String tokenKey = AuthRedisKeyHelper.refreshToken(refreshTokenSession.getRefreshTokenHash());
        redisTemplate.opsForValue().set(tokenKey, RefreshTokenSnapshot.fromDomain(refreshTokenSession),
                ttl(refreshTokenSession.getExpireAt()));
        redisTemplate.opsForSet().add(AuthRedisKeyHelper.sessionRefreshTokens(refreshTokenSession.getSessionId()),
                refreshTokenSession.getRefreshTokenHash());
        redisTemplate.expire(AuthRedisKeyHelper.sessionRefreshTokens(refreshTokenSession.getSessionId()),
                ttl(refreshTokenSession.getExpireAt()));
        return refreshTokenSession;
    }

    @Override
    public Optional<RefreshTokenSession> findRefreshTokenByHash(String refreshTokenHash) {
        return readValue(AuthRedisKeyHelper.refreshToken(refreshTokenHash), RefreshTokenSnapshot.class)
                .map(RefreshTokenSnapshot::toDomain);
    }

    @Override
    public void invalidateRefreshTokensBySessionId(String sessionId) {
        Set<Object> tokenHashes = redisTemplate.opsForSet().members(AuthRedisKeyHelper.sessionRefreshTokens(sessionId));
        if (tokenHashes == null || tokenHashes.isEmpty()) {
            return;
        }
        tokenHashes.stream()
                .map(String::valueOf)
                .forEach(hash -> findRefreshTokenByHash(hash).ifPresent(token -> {
                    token.invalidate();
                    saveRefreshToken(token);
                }));
    }

    private List<AuthSession> loadSessionsByIndex(String indexKey) {
        Set<Object> sessionIds = redisTemplate.opsForSet().members(indexKey);
        if (sessionIds == null || sessionIds.isEmpty()) {
            return List.of();
        }
        return sessionIds.stream()
                .map(String::valueOf)
                .map(this::findSessionBySessionId)
                .flatMap(Optional::stream)
                .toList();
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
    private static class SessionSnapshot {
        private Long id;
        private String sessionId;
        private String tenantNo;
        private String userId;
        private String identityId;
        private String identityType;
        private String loginType;
        private Instant issuedAt;
        private Instant expireAt;
        private String sessionStatus;
        private Instant lastAccessTime;
        private Instant logoutAt;
        private String invalidateReason;

        private static SessionSnapshot fromDomain(AuthSession authSession) {
            return new SessionSnapshot(authSession.getId(), authSession.getSessionId(), authSession.getTenantNo(),
                    authSession.getUserId(), authSession.getIdentityId(), authSession.getIdentityType(),
                    authSession.getLoginType(), authSession.getIssuedAt(), authSession.getExpireAt(),
                    authSession.getSessionStatus(), authSession.getLastAccessTime(), authSession.getLogoutAt(),
                    authSession.getInvalidateReason());
        }

        private AuthSession toDomain() {
            AuthSession authSession = new AuthSession(id, sessionId, tenantNo, userId, identityId, identityType,
                    loginType, issuedAt, expireAt);
            if (!"ACTIVE".equals(sessionStatus)) {
                if ("LOGGED_OUT".equals(sessionStatus) && logoutAt != null) {
                    authSession.logout(logoutAt);
                } else if ("INVALIDATED".equals(sessionStatus)) {
                    authSession.invalidate(invalidateReason);
                } else if ("EXPIRED".equals(sessionStatus)) {
                    authSession.expire();
                }
            }
            if (lastAccessTime != null) {
                authSession.touch(lastAccessTime);
            }
            return authSession;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class RefreshTokenSnapshot {
        private String sessionId;
        private String refreshTokenHash;
        private Instant issuedAt;
        private Instant expireAt;
        private String tokenStatus;
        private Instant usedAt;

        private static RefreshTokenSnapshot fromDomain(RefreshTokenSession refreshTokenSession) {
            return new RefreshTokenSnapshot(refreshTokenSession.getSessionId(), refreshTokenSession.getRefreshTokenHash(),
                    refreshTokenSession.getIssuedAt(), refreshTokenSession.getExpireAt(),
                    refreshTokenSession.getTokenStatus(), refreshTokenSession.getUsedAt());
        }

        private RefreshTokenSession toDomain() {
            RefreshTokenSession refreshTokenSession = new RefreshTokenSession(sessionId, refreshTokenHash, issuedAt, expireAt);
            if ("USED".equals(tokenStatus) && usedAt != null) {
                refreshTokenSession.markUsed(usedAt);
            } else if ("INVALIDATED".equals(tokenStatus)) {
                refreshTokenSession.invalidate();
            } else if ("EXPIRED".equals(tokenStatus)) {
                refreshTokenSession.expire();
            }
            return refreshTokenSession;
        }
    }
}
