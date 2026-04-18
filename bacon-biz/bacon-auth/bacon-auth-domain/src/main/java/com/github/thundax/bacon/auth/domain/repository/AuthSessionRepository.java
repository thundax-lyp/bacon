package com.github.thundax.bacon.auth.domain.repository;

import com.github.thundax.bacon.auth.domain.model.entity.AuthSession;
import com.github.thundax.bacon.auth.domain.model.entity.RefreshTokenSession;
import java.util.List;
import java.util.Optional;

public interface AuthSessionRepository {

    AuthSession update(AuthSession authSession);

    Optional<AuthSession> findBySessionId(String sessionId);

    List<AuthSession> listByTenantIdAndUserId(Long tenantId, Long userId);

    List<AuthSession> listByTenantId(Long tenantId);

    RefreshTokenSession update(RefreshTokenSession refreshTokenSession);

    Optional<RefreshTokenSession> findByHash(String refreshTokenHash);

    void markInvalidBySessionId(String sessionId);
}
