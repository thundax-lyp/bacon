package com.github.thundax.bacon.auth.api.facade;

public interface SessionCommandFacade {

    void invalidateUserSessions(String tenantId, String userId, String reason);

    void invalidateTenantSessions(String tenantId, String reason);

    void invalidateSession(String sessionId, String reason);
}
