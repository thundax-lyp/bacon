package com.github.thundax.bacon.auth.api.facade;

import com.github.thundax.bacon.auth.api.dto.CurrentSessionResponse;
import com.github.thundax.bacon.auth.api.dto.SessionValidationResponse;

public interface TokenVerifyFacade {

    SessionValidationResponse verifyAccessToken(String accessToken);

    CurrentSessionResponse getSessionContext(String sessionId);
}
