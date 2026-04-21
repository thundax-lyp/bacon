package com.github.thundax.bacon.auth.interfaces.facade;

import com.github.thundax.bacon.auth.api.facade.TokenVerifyFacade;
import com.github.thundax.bacon.auth.api.request.SessionContextGetFacadeRequest;
import com.github.thundax.bacon.auth.api.request.TokenVerifyFacadeRequest;
import com.github.thundax.bacon.auth.api.response.CurrentSessionFacadeResponse;
import com.github.thundax.bacon.auth.api.response.SessionValidationFacadeResponse;
import com.github.thundax.bacon.auth.application.query.SessionContextQuery;
import com.github.thundax.bacon.auth.application.query.TokenQueryApplicationService;
import com.github.thundax.bacon.auth.application.query.TokenVerifyQuery;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "mono", matchIfMissing = true)
public class TokenVerifyFacadeLocalImpl implements TokenVerifyFacade {

    private final TokenQueryApplicationService tokenQueryApplicationService;

    public TokenVerifyFacadeLocalImpl(TokenQueryApplicationService tokenQueryApplicationService) {
        this.tokenQueryApplicationService = tokenQueryApplicationService;
    }

    @Override
    public SessionValidationFacadeResponse verifyAccessToken(TokenVerifyFacadeRequest request) {
        return SessionValidationFacadeResponse.from(
                tokenQueryApplicationService.verifyAccessToken(new TokenVerifyQuery(request.getAccessToken())));
    }

    @Override
    public CurrentSessionFacadeResponse getSessionContext(SessionContextGetFacadeRequest request) {
        var session = tokenQueryApplicationService.getSessionContext(new SessionContextQuery(request.getSessionId()));
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
