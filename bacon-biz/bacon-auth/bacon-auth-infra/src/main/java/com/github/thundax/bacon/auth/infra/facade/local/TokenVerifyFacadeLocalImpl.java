package com.github.thundax.bacon.auth.infra.facade.local;

import com.github.thundax.bacon.auth.api.dto.CurrentSessionDTO;
import com.github.thundax.bacon.auth.api.dto.SessionValidationDTO;
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
    public SessionValidationDTO verifyAccessToken(String accessToken) {
        return tokenApplicationService.verifyAccessToken(accessToken);
    }

    @Override
    public CurrentSessionDTO getSessionContext(String sessionId) {
        return tokenApplicationService.getSessionContext(sessionId);
    }
}
