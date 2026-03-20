package com.github.thundax.bacon.auth.infra.repository.impl;

import com.github.thundax.bacon.auth.domain.entity.AuthSession;
import com.github.thundax.bacon.auth.domain.entity.RefreshTokenSession;
import com.github.thundax.bacon.auth.domain.repository.AuthSessionRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class AuthSessionRepositoryImpl implements AuthSessionRepository {

    private final InMemoryAuthStore authStore;

    public AuthSessionRepositoryImpl(InMemoryAuthStore authStore) {
        this.authStore = authStore;
    }

    @Override
    public AuthSession saveSession(AuthSession authSession) {
        authStore.getSessions().put(authSession.getSessionId(), authSession);
        return authSession;
    }

    @Override
    public Optional<AuthSession> findSessionBySessionId(String sessionId) {
        return Optional.ofNullable(authStore.getSessions().get(sessionId));
    }

    @Override
    public List<AuthSession> findSessionsByTenantIdAndUserId(Long tenantId, Long userId) {
        return authStore.getSessions().values().stream()
                .filter(session -> tenantId.equals(session.getTenantId()))
                .filter(session -> userId.equals(session.getUserId()))
                .toList();
    }

    @Override
    public List<AuthSession> findSessionsByTenantId(Long tenantId) {
        return authStore.getSessions().values().stream()
                .filter(session -> tenantId.equals(session.getTenantId()))
                .toList();
    }

    @Override
    public RefreshTokenSession saveRefreshToken(RefreshTokenSession refreshTokenSession) {
        authStore.getRefreshTokenSessions().put(refreshTokenSession.getRefreshTokenHash(), refreshTokenSession);
        return refreshTokenSession;
    }

    @Override
    public Optional<RefreshTokenSession> findRefreshTokenByHash(String refreshTokenHash) {
        return Optional.ofNullable(authStore.getRefreshTokenSessions().get(refreshTokenHash));
    }

    @Override
    public void invalidateRefreshTokensBySessionId(String sessionId) {
        authStore.getRefreshTokenSessions().values().stream()
                .filter(token -> sessionId.equals(token.getSessionId()))
                .forEach(RefreshTokenSession::invalidate);
    }
}
