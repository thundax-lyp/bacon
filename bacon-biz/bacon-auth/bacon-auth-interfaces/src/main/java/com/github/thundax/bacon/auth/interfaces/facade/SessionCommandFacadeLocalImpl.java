package com.github.thundax.bacon.auth.interfaces.facade;

import com.github.thundax.bacon.auth.api.facade.SessionCommandFacade;
import com.github.thundax.bacon.auth.api.request.SessionInvalidateFacadeRequest;
import com.github.thundax.bacon.auth.api.request.SessionInvalidateTenantFacadeRequest;
import com.github.thundax.bacon.auth.api.request.SessionInvalidateUserFacadeRequest;
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
    public void invalidateUserSessions(SessionInvalidateUserFacadeRequest request) {
        sessionApplicationService.invalidateUserSessions(request.getTenantId(), request.getUserId(), request.getReason());
    }

    @Override
    public void invalidateTenantSessions(SessionInvalidateTenantFacadeRequest request) {
        sessionApplicationService.invalidateTenantSessions(request.getTenantId(), request.getReason());
    }

    @Override
    public void invalidateSession(SessionInvalidateFacadeRequest request) {
        sessionApplicationService.invalidateSession(request.getSessionId(), request.getReason());
    }
}
