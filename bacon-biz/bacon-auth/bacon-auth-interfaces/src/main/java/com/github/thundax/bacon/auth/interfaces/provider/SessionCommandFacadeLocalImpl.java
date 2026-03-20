package com.github.thundax.bacon.auth.interfaces.provider;

import com.github.thundax.bacon.auth.api.facade.SessionCommandFacade;
import com.github.thundax.bacon.auth.application.service.SessionApplicationService;
import org.springframework.stereotype.Component;

@Component
public class SessionCommandFacadeLocalImpl implements SessionCommandFacade {

    private final SessionApplicationService sessionApplicationService;

    public SessionCommandFacadeLocalImpl(SessionApplicationService sessionApplicationService) {
        this.sessionApplicationService = sessionApplicationService;
    }

    @Override
    public void invalidateUserSessions(Long tenantId, Long userId, String reason) {
        sessionApplicationService.invalidateUserSessions(tenantId, userId, reason);
    }

    @Override
    public void invalidateTenantSessions(Long tenantId, String reason) {
        sessionApplicationService.invalidateTenantSessions(tenantId, reason);
    }

    @Override
    public void invalidateSession(String sessionId, String reason) {
        sessionApplicationService.invalidateSession(sessionId, reason);
    }
}
