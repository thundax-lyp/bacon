package com.github.thundax.bacon.auth.domain.repository;

import com.github.thundax.bacon.auth.domain.model.entity.AuthSession;
import com.github.thundax.bacon.auth.domain.model.entity.RefreshTokenSession;

import java.util.List;
import java.util.Optional;

public interface AuthSessionRepository {

    AuthSession saveSession(AuthSession authSession);

    Optional<AuthSession> findSessionBySessionId(String sessionId);

    List<AuthSession> findSessionsByTenantNoAndUserId(String tenantNo, String userId);

    List<AuthSession> findSessionsByTenantNo(String tenantNo);

    RefreshTokenSession saveRefreshToken(RefreshTokenSession refreshTokenSession);

    Optional<RefreshTokenSession> findRefreshTokenByHash(String refreshTokenHash);

    void invalidateRefreshTokensBySessionId(String sessionId);
}
