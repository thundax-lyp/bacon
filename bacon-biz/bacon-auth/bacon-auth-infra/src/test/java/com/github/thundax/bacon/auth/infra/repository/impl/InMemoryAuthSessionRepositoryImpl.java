package com.github.thundax.bacon.auth.infra.repository.impl;

import com.github.thundax.bacon.auth.domain.model.entity.AuthSession;
import com.github.thundax.bacon.auth.domain.model.entity.RefreshTokenSession;
import com.github.thundax.bacon.auth.domain.repository.AuthSessionRepository;
import org.springframework.stereotype.Repository;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.Optional;

@Repository
@Profile("test")
public class InMemoryAuthSessionRepositoryImpl implements AuthSessionRepository {

    private final TestAuthMemoryStore authStore;

    public InMemoryAuthSessionRepositoryImpl(TestAuthMemoryStore authStore) {
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
    public List<AuthSession> findSessionsByTenantNoAndUserId(String tenantNo, Long userId) {
        return authStore.getSessions().values().stream()
                .filter(session -> tenantNo.equals(session.getTenantNo()))
                .filter(session -> userId.equals(session.getUserId()))
                .toList();
    }

    @Override
    public List<AuthSession> findSessionsByTenantNo(String tenantNo) {
        return authStore.getSessions().values().stream()
                .filter(session -> tenantNo.equals(session.getTenantNo()))
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
