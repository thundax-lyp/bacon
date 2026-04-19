package com.github.thundax.bacon.auth.interfaces.facade;

import com.github.thundax.bacon.auth.api.facade.TokenVerifyFacade;
import com.github.thundax.bacon.auth.api.request.SessionContextGetFacadeRequest;
import com.github.thundax.bacon.auth.api.request.TokenVerifyFacadeRequest;
import com.github.thundax.bacon.auth.api.response.CurrentSessionFacadeResponse;
import com.github.thundax.bacon.auth.api.response.SessionValidationFacadeResponse;
import com.github.thundax.bacon.auth.application.command.TokenApplicationService;
import com.github.thundax.bacon.auth.application.dto.CurrentSessionDTO;
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
        CurrentSessionDTO session = tokenApplicationService.getSessionContext(request.getSessionId());
        return CurrentSessionFacadeResponse.from(
                session.getSessionId(),
                session.getTenantId(),
                session.getUserId(),
                session.getIdentityType(),
                session.getLoginType(),
                session.getSessionStatus(),
                session.getIssuedAt(),
                session.getLastAccessTime(),
                session.getExpireAt());
    }
}
