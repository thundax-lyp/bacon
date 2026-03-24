package com.github.thundax.bacon.auth.api.facade;

import com.github.thundax.bacon.auth.api.dto.CurrentSessionDTO;
import com.github.thundax.bacon.auth.api.dto.SessionValidationDTO;

public interface TokenVerifyFacade {

    SessionValidationDTO verifyAccessToken(String accessToken);

    CurrentSessionDTO getSessionContext(String sessionId);
}
