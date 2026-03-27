package com.github.thundax.bacon.auth.infra.facade.remote;

import com.github.thundax.bacon.common.core.config.RestClientFactory;
import com.github.thundax.bacon.auth.api.dto.OAuthClientDTO;
import com.github.thundax.bacon.auth.api.facade.OAuthClientReadFacade;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "bacon.runtime.mode", havingValue = "micro")
public class OAuthClientReadFacadeRemoteImpl implements OAuthClientReadFacade {

    private final RestClient restClient;

    public OAuthClientReadFacadeRemoteImpl(RestClientFactory restClientFactory,
                                           @Value("${bacon.remote.auth-base-url:http://127.0.0.1:8081/api}") String baseUrl) {
        this.restClient = restClientFactory.create(baseUrl);
    }

    @Override
    public OAuthClientDTO getClientByClientId(String clientId) {
        return restClient.get()
                .uri("/providers/auth/oauth-clients/{clientId}", clientId)
                .retrieve()
                .body(OAuthClientDTO.class);
    }
}
