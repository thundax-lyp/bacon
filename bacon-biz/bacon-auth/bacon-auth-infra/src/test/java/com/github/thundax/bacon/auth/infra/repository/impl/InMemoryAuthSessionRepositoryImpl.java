package com.github.thundax.bacon.auth.infra.repository.impl;

import com.github.thundax.bacon.auth.domain.model.entity.AuthSession;
import com.github.thundax.bacon.auth.domain.model.entity.RefreshTokenSession;
import com.github.thundax.bacon.auth.domain.repository.AuthSessionRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("test")
public class InMemoryAuthSessionRepositoryImpl implements AuthSessionRepository {

    private final TestAuthMemoryStore authStore;

    public InMemoryAuthSessionRepositoryImpl(TestAuthMemoryStore authStore) {
        this.authStore = authStore;
    }

    @Override
    public AuthSession update(AuthSession authSession) {
        authStore.getSessions().put(authSession.getSessionId(), authSession);
        return authSession;
    }

    @Override
    public Optional<AuthSession> findBySessionId(String sessionId) {
        return Optional.ofNullable(authStore.getSessions().get(sessionId));
    }

    @Override
    public List<AuthSession> listByTenantIdAndUserId(Long tenantId, Long userId) {
        return authStore.getSessions().values().stream()
                .filter(session -> tenantId.equals(session.getTenantIdValue()))
                .filter(session -> session.getUserId() != null
                        && userId.equals(session.getUserId().value()))
                .toList();
    }

    @Override
    public List<AuthSession> listByTenantId(Long tenantId) {
        return authStore.getSessions().values().stream()
                .filter(session -> tenantId.equals(session.getTenantIdValue()))
                .toList();
    }

    @Override
    public RefreshTokenSession update(RefreshTokenSession refreshTokenSession) {
        authStore.getRefreshTokenSessions().put(refreshTokenSession.getRefreshTokenHash(), refreshTokenSession);
        return refreshTokenSession;
    }

    @Override
    public Optional<RefreshTokenSession> findByHash(String refreshTokenHash) {
        return Optional.ofNullable(authStore.getRefreshTokenSessions().get(refreshTokenHash));
    }

    @Override
    public void markInvalidBySessionId(String sessionId) {
        authStore.getRefreshTokenSessions().values().stream()
                .filter(token -> sessionId.equals(token.getSessionIdValue()))
                .forEach(RefreshTokenSession::invalidate);
    }
}
