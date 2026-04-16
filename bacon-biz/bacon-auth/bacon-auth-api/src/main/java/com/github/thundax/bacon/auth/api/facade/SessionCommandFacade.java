package com.github.thundax.bacon.auth.api.facade;

import com.github.thundax.bacon.auth.api.request.SessionInvalidateFacadeRequest;
import com.github.thundax.bacon.auth.api.request.SessionInvalidateTenantFacadeRequest;
import com.github.thundax.bacon.auth.api.request.SessionInvalidateUserFacadeRequest;

public interface SessionCommandFacade {

    void invalidateUserSessions(SessionInvalidateUserFacadeRequest request);

    void invalidateTenantSessions(SessionInvalidateTenantFacadeRequest request);

    void invalidateSession(SessionInvalidateFacadeRequest request);
}
