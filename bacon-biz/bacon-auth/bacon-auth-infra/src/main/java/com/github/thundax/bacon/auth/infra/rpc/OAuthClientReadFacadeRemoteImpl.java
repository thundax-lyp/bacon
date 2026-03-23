package com.github.thundax.bacon.auth.infra.rpc;

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

    public OAuthClientReadFacadeRemoteImpl(@Value("${bacon.remote.auth-base-url:http://localhost:8081/api}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Override
    public OAuthClientDTO getClientByClientId(String clientId) {
        return restClient.get()
                .uri("/providers/auth/oauth-clients/{clientId}", clientId)
                .retrieve()
                .body(OAuthClientDTO.class);
    }
}
