package com.github.thundax.bacon.auth.infra.rpc;

import com.github.thundax.bacon.auth.api.dto.CurrentSessionResponse;
import com.github.thundax.bacon.auth.api.dto.SessionValidationResponse;
import com.github.thundax.bacon.auth.api.facade.TokenVerifyFacade;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
public class TokenVerifyFacadeRemoteImpl implements TokenVerifyFacade {

    private final RestClient restClient;

    public TokenVerifyFacadeRemoteImpl(@Value("${bacon.remote.auth-base-url:http://127.0.0.1:8081/api}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public SessionValidationResponse verifyAccessToken(String accessToken) {
        return restClient.get()
                .uri("/providers/auth/tokens/verify?accessToken={accessToken}", accessToken)
                .retrieve()
                .body(SessionValidationResponse.class);
    }

    @Override
    public CurrentSessionResponse getSessionContext(String sessionId) {
        return restClient.get()
                .uri("/providers/auth/sessions/{sessionId}", sessionId)
                .retrieve()
                .body(CurrentSessionResponse.class);
    }
}
