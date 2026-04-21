package com.github.thundax.bacon.auth.interfaces.facade;

import com.github.thundax.bacon.auth.api.facade.SessionCommandFacade;
import com.github.thundax.bacon.auth.api.request.SessionInvalidateFacadeRequest;
import com.github.thundax.bacon.auth.api.request.SessionInvalidateTenantFacadeRequest;
import com.github.thundax.bacon.auth.api.request.SessionInvalidateUserFacadeRequest;
import com.github.thundax.bacon.auth.application.command.SessionCommandApplicationService;
import com.github.thundax.bacon.auth.application.command.SessionInvalidateCommand;
import com.github.thundax.bacon.auth.application.command.SessionInvalidateTenantCommand;
import com.github.thundax.bacon.auth.application.command.SessionInvalidateUserCommand;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
public class SessionCommandFacadeLocalImpl implements SessionCommandFacade {

    private final SessionCommandApplicationService sessionCommandApplicationService;

    public SessionCommandFacadeLocalImpl(SessionCommandApplicationService sessionCommandApplicationService) {
        this.sessionCommandApplicationService = sessionCommandApplicationService;
    }

    @Override
    public void invalidateUserSessions(SessionInvalidateUserFacadeRequest request) {
        sessionCommandApplicationService.invalidateUserSessions(
                new SessionInvalidateUserCommand(request.getTenantId(), request.getUserId(), request.getReason()));
    }

    @Override
    public void invalidateTenantSessions(SessionInvalidateTenantFacadeRequest request) {
        sessionCommandApplicationService.invalidateTenantSessions(
                new SessionInvalidateTenantCommand(request.getTenantId(), request.getReason()));
    }

    @Override
    public void invalidateSession(SessionInvalidateFacadeRequest request) {
        sessionCommandApplicationService.invalidateSession(
                new SessionInvalidateCommand(request.getSessionId(), request.getReason()));
    }
}
