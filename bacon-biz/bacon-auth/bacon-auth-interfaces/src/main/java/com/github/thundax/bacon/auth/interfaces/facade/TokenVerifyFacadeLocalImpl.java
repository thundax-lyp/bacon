package com.github.thundax.bacon.auth.interfaces.facade;

import com.github.thundax.bacon.auth.api.facade.TokenVerifyFacade;
import com.github.thundax.bacon.auth.api.request.SessionContextGetFacadeRequest;
import com.github.thundax.bacon.auth.api.request.TokenVerifyFacadeRequest;
import com.github.thundax.bacon.auth.api.response.CurrentSessionFacadeResponse;
import com.github.thundax.bacon.auth.api.response.SessionValidationFacadeResponse;
import com.github.thundax.bacon.auth.application.command.TokenApplicationService;
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
    public SessionValidationFacadeResponse verifyAccessToken(TokenVerifyFacadeRequest request) {
        return SessionValidationFacadeResponse.from(tokenApplicationService.verifyAccessToken(request.getAccessToken()));
    }

    @Override
    public CurrentSessionFacadeResponse getSessionContext(SessionContextGetFacadeRequest request) {
        return CurrentSessionFacadeResponse.from(tokenApplicationService.getSessionContext(request.getSessionId()));
    }
}
