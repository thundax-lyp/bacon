package com.github.thundax.bacon.auth.api.facade;

import com.github.thundax.bacon.auth.api.request.SessionContextGetFacadeRequest;
import com.github.thundax.bacon.auth.api.request.TokenVerifyFacadeRequest;
import com.github.thundax.bacon.auth.api.response.CurrentSessionFacadeResponse;
import com.github.thundax.bacon.auth.api.response.SessionValidationFacadeResponse;

public interface TokenVerifyFacade {

    SessionValidationFacadeResponse verifyAccessToken(TokenVerifyFacadeRequest request);

    CurrentSessionFacadeResponse getSessionContext(SessionContextGetFacadeRequest request);
}
