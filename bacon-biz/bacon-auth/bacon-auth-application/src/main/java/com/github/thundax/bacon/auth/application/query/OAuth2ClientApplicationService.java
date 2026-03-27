package com.github.thundax.bacon.auth.application.query;

import com.github.thundax.bacon.auth.api.dto.OAuthClientDTO;
import com.github.thundax.bacon.auth.domain.model.entity.OAuthClient;
import com.github.thundax.bacon.auth.domain.repository.OAuthClientRepository;
import org.springframework.stereotype.Service;

@Service
public class OAuth2ClientApplicationService {

    private final OAuthClientRepository oAuthClientRepository;

    public OAuth2ClientApplicationService(OAuthClientRepository oAuthClientRepository) {
        this.oAuthClientRepository = oAuthClientRepository;
    }

    public OAuthClientDTO getClientByClientId(String clientId) {
        OAuthClient client = oAuthClientRepository.findByClientId(clientId)
                .orElseThrow(() -> new IllegalArgumentException("OAuth client not found: " + clientId));
        return new OAuthClientDTO(client.getClientId(), client.getClientName(), client.getGrantTypes(),
                client.getScopes(), client.getRedirectUris(), client.isEnabled());
    }
}
