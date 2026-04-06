package com.github.thundax.bacon.auth.api.facade;

public interface SessionCommandFacade {

    void invalidateUserSessions(Long tenantId, Long userId, String reason);

    void invalidateTenantSessions(Long tenantId, String reason);

    void invalidateSession(String sessionId, String reason);
}
