package com.github.thundax.bacon.auth.infra.facade.remote;

import com.github.thundax.bacon.common.core.config.RestClientFactory;
import com.github.thundax.bacon.auth.api.dto.CurrentSessionDTO;
import com.github.thundax.bacon.auth.api.dto.SessionValidationDTO;
import com.github.thundax.bacon.auth.api.facade.TokenVerifyFacade;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
public class TokenVerifyFacadeRemoteImpl implements TokenVerifyFacade {

    private final RestClient restClient;

    public TokenVerifyFacadeRemoteImpl(RestClientFactory restClientFactory,
                                       @Value("${bacon.remote.auth-base-url:http://127.0.0.1:8081/api}") String baseUrl) {
        this.restClient = restClientFactory.create(baseUrl);
    }

    @Override
    public SessionValidationDTO verifyAccessToken(String accessToken) {
        return restClient.get()
                .uri("/providers/auth/tokens/verify?accessToken={accessToken}", accessToken)
                .retrieve()
                .body(SessionValidationDTO.class);
    }

    @Override
    public CurrentSessionDTO getSessionContext(String sessionId) {
        return restClient.get()
                .uri("/providers/auth/sessions/{sessionId}", sessionId)
                .retrieve()
                .body(CurrentSessionDTO.class);
    }
}
