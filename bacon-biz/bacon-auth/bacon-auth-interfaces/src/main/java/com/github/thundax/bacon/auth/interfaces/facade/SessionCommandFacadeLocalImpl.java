package com.github.thundax.bacon.auth.interfaces.facade;

import com.github.thundax.bacon.auth.api.facade.SessionCommandFacade;
import com.github.thundax.bacon.auth.application.command.SessionApplicationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
public class SessionCommandFacadeLocalImpl implements SessionCommandFacade {

    private final SessionApplicationService sessionApplicationService;

    public SessionCommandFacadeLocalImpl(SessionApplicationService sessionApplicationService) {
        this.sessionApplicationService = sessionApplicationService;
    }

    @Override
    public void invalidateUserSessions(String tenantId, String userId, String reason) {
        sessionApplicationService.invalidateUserSessions(tenantId, userId, reason);
    }

    @Override
    public void invalidateTenantSessions(String tenantId, String reason) {
        sessionApplicationService.invalidateTenantSessions(tenantId, reason);
    }

    @Override
    public void invalidateSession(String sessionId, String reason) {
        sessionApplicationService.invalidateSession(sessionId, reason);
    }
}
