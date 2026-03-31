package com.github.thundax.bacon.auth.api.facade;

public interface SessionCommandFacade {

    void invalidateUserSessions(String tenantNo, String userId, String reason);

    void invalidateTenantSessions(String tenantNo, String reason);

    void invalidateSession(String sessionId, String reason);
}
