package com.github.thundax.bacon.auth.interfaces.facade;

import com.github.thundax.bacon.auth.api.dto.CurrentSessionResponse;
import com.github.thundax.bacon.auth.api.dto.SessionValidationResponse;
import com.github.thundax.bacon.auth.api.facade.TokenVerifyFacade;
import com.github.thundax.bacon.auth.application.service.TokenApplicationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
public class TokenVerifyFacadeLocalImpl implements TokenVerifyFacade {

    private final TokenApplicationService tokenApplicationService;

    public TokenVerifyFacadeLocalImpl(TokenApplicationService tokenApplicationService) {
        this.tokenApplicationService = tokenApplicationService;
    }

    @Override
    public SessionValidationResponse verifyAccessToken(String accessToken) {
        return tokenApplicationService.verifyAccessToken(accessToken);
    }

    @Override
    public CurrentSessionResponse getSessionContext(String sessionId) {
        return tokenApplicationService.getSessionContext(sessionId);
    }
}
